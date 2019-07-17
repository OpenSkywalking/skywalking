/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.core.analysis.metrics.expression;

/**
 * BinaryMatchExpression accepts two calculate factors,
 * and return the True/False result.
 *
 * @author wusheng
 */
public abstract class BinaryMatchExpression {
    protected Object left;
    protected Object right;

    public BinaryMatchExpression setLeft(Object left) {
        this.left = left;
        return this;
    }

    public BinaryMatchExpression setLeft(boolean left) {
        return setLeft(left);
    }

    public BinaryMatchExpression setLeft(int left) {
        return setLeft(left);
    }

    public BinaryMatchExpression setLeft(long left) {
        return setLeft(left);
    }

    public BinaryMatchExpression setLeft(double left) {
        return setLeft(left);
    }

    public BinaryMatchExpression setRight(Object right) {
        this.right = right;
        return this;
    }

    public BinaryMatchExpression setRight(boolean left) {
        return setLeft(left);
    }

    public BinaryMatchExpression setRight(int left) {
        return setLeft(left);
    }

    public BinaryMatchExpression setRight(long left) {
        return setLeft(left);
    }

    public BinaryMatchExpression setRight(double left) {
        return setLeft(left);
    }

    public abstract boolean match();
}
