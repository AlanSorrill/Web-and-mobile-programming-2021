package alansorrill.networks.codeprojectone;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;



public class Promise<T> {
    
    private boolean failed = false;
    private boolean completed = false;
    private T value = null;
    private Exception err = null;
    private List<Function<T, Void>> succListeners = new LinkedList<>();
    public Promise<T> then(Function<T, Void> callback){
        if(completed){
            callback.apply(value);
            return this;
        }
        succListeners.add(callback);
        return this;
    }
    private List<Function<Exception, Void>> errListeners = new LinkedList<>();
    public Promise<T> catchErr(Function<Exception, Void> callback){
        if(failed){
            callback.apply(err);
            return this;
        }
        errListeners.add(callback);
        return this;
    }

    public void accept(T t){
        value = t;
        completed = true;
        succListeners.forEach( new Consumer<Function<T, Void>>(){

            @Override
            public void accept(Function<T, Void> listener) {
                listener.apply(value);
            }
            
        });
    }
    public void reject(Exception t){
        err = t;
        failed = true;
        errListeners.forEach( new Consumer<Function<Exception, Void>>(){

            @Override
            public void accept(Function<Exception, Void> listener) {
                listener.apply(err);
            }
            
        });
    }
}
