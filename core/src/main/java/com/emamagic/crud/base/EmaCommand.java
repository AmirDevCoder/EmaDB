package com.emamagic.crud.base;

public interface EmaCommand<T, R> {
    R execute(T entity);
}
