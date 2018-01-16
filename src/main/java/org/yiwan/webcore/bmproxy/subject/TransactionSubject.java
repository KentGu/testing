package org.yiwan.webcore.bmproxy.subject;

import org.yiwan.webcore.bmproxy.observer.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kenny Wang on 3/14/2016.
 */
public class TransactionSubject implements Subject {

    private List<Observer> observers = new ArrayList<>();

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void nodifyObserversStart() {
        for (Observer observer : observers) {
            observer.start();
        }
    }

    @Override
    public void nodifyObserversStop() {
        for (Observer observer : observers) {
            observer.stop();
        }
    }
}
