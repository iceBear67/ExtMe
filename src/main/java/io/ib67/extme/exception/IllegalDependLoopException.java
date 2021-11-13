package io.ib67.extme.exception;

public class IllegalDependLoopException extends IllegalStateException{
    public IllegalDependLoopException(String s) {
        super(s);
    }
}
