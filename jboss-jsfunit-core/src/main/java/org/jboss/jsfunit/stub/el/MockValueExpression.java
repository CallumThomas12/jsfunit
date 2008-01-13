/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jsfunit.stub.el;

import java.util.ArrayList;
import java.util.List;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 * <p>Mock implementation of <code>ValueExpression</code>.</p>
 *
 * <p>This implementation supports a limited subset of overall expression functionality:</p>
 * <ul>
 * <li>A literal string that contains no expression delimiters.</li>
 * <li>An expression that starts with "#{" or "${", and ends with "}".</li>
 * </ul>
 */
public class MockValueExpression extends ValueExpression {
    

    // ------------------------------------------------------------ Constructors


    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -8649071428507512623L;


    /**
     * <p>Construct a new expression for the specified expression string.</p>
     *
     * @param expression Expression string to be evaluated
     * @param expectedType Expected type of the result
     */
    public MockValueExpression(String expression, Class expectedType) {

        if (expression == null) {
            throw new NullPointerException("Expression string cannot be null");
        }
        this.expression = expression;
        this.expectedType = expectedType;
        parse();

    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>The parsed elements of this expression.</p>
     */
    private String[] elements = null;


    /**
     * <p>The expected result type for <code>getValue()</code> calls.</p>
     */
    private Class expectedType = null;


    /**
     * <p>The original expression string used to create this expression.</p>
     */
    private String expression = null;


    // ------------------------------------------------------ Expression Methods


    /**
     * <p>Return <code>true</code> if this expression is equal to the
     * specified expression.</p>
     *
     * @param obj Object to be compared
     */
    public boolean equals(Object obj) {

        if ((obj != null) & (obj instanceof ValueExpression)) {
            return expression.equals(((ValueExpression) obj).getExpressionString());
        } else {
            return false;
        }

    }


    /**
     * <p>Return the original String used to create this expression,
     * unmodified.</p>
     */
    public String getExpressionString() {

        return this.expression;

    }


    /**
     * <p>Return the hash code for this expression.</p>
     */
    public int hashCode() {

        return this.expression.hashCode();

    }


    /**
     * <p>Return <code>true</code> if the expression string for this expression
     * contains only literal text.</p>
     */
    public boolean isLiteralText() {

        return (expression.indexOf("${") < 0) && (expression.indexOf("#{") < 0);

    }


    // ------------------------------------------------- ValueExpression Methods


    /**
     * <p>Return the type that the result of this expression will
     * be coerced to.</p>
     */
    public Class getExpectedType() {

        return this.expectedType;

    }


    /**
     * <p>Evaluate this expression relative to the specified context,
     * and return the most general type that is acceptable for the
     * value passed in a <code>setValue()</code> call.</p>
     *
     * @param context ELContext for this evaluation
     */
    public Class getType(ELContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        Object value = getValue(context);
        if (value == null) {
            return null;
        } else {
            return value.getClass();
        }

    }


    /**
     * <p>Evaluate this expression relative to the specified context,
     * and return the result.</p>
     *
     * @param context ELContext for this evaluation
     */
    public Object getValue(ELContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (isLiteralText()) {
            return expression;
        }

        FacesContext fcontext = (FacesContext) context.getContext(FacesContext.class);
        ELResolver resolver = fcontext.getApplication().getELResolver();
        Object base = null;
        for (int i = 0; i < elements.length; i++) {
            base = resolver.getValue(context, base, elements[i]);
        }
        return fcontext.getApplication().getExpressionFactory().coerceToType(base, getExpectedType());

    }


    /**
     * <p>Evaluate this expression relative to the specified context,
     * and return <code>true</code> if a call to <code>setValue()</code>
     * will always fail.</p>
     *
     * @param context ELContext for this evaluation
     */
    public boolean isReadOnly(ELContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (isLiteralText()) {
            return true;
        }

        FacesContext fcontext = (FacesContext) context.getContext(FacesContext.class);
        ELResolver resolver = fcontext.getApplication().getELResolver();
        Object base = null;
        for (int i = 0; i < elements.length - 1; i++) {
            base = resolver.getValue(context, base, elements[i]);
        }
        return resolver.isReadOnly(context, base, elements[elements.length - 1]);

    }



    /**
     * <p>Evaluate this expression relative to the specified context,
     * and set the result to the specified value.</p>
     *
     * @param context ELContext for this evaluation
     * @param value Value to which the result should be set
     */
    public void setValue(ELContext context, Object value) {

        if (context == null) {
            throw new NullPointerException();
        }

        FacesContext fcontext = (FacesContext) context.getContext(FacesContext.class);
        ELResolver resolver = fcontext.getApplication().getELResolver();
        Object base = null;
        for (int i = 0; i < elements.length - 1; i++) {
            base = resolver.getValue(context, base, elements[i]);
        }
        resolver.setValue(context, base, elements[elements.length - 1], value);

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Parse the expression string into its constituent elemetns.</p>
     */
    private void parse() {

        if (isLiteralText()) {
            elements = new String[0];
            return;
        }

        if (expression.startsWith("${") || expression.startsWith("#{")) {
            if (expression.endsWith("}")) {               
                List names = new ArrayList();
                StringBuffer expr = new StringBuffer(expression.substring(2, expression.length() - 1).replaceAll(" ", ""));
                boolean isBlockOn = false;
                for (int i = expr.length() - 1; i > -1; i--) {
                    if (expr.charAt(i) == ' ') {
                        expr.deleteCharAt(i);
                    } else if (expr.charAt(i) == ']') {
                        expr.deleteCharAt(i);
                    } else if (expr.charAt(i) == '[') {
                        expr.deleteCharAt(i);
                    } else if (expr.charAt(i) == '\'') {
                        if (!isBlockOn) {
                            expr.deleteCharAt(i);
                        } else {
                            names.add(0, expr.substring(i + 1));
                            expr.delete(i, expr.length());
                        }
                        isBlockOn = !isBlockOn;
                    } else if (expr.charAt(i) == '.' && !isBlockOn) {
                        names.add(0, expr.substring(i + 1));
                        expr.delete(i, expr.length());
                    }
                }
                if (expr.length() > 0) {
                    names.add(0, expr.toString());
                }

                elements = (String[]) names.toArray(new String[names.size()]);
            } else {
                throw new IllegalArgumentException(expression);
            }
        } else {
            throw new IllegalArgumentException(expression);
        }

    }


}
