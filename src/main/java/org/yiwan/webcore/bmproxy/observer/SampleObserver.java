package org.yiwan.webcore.bmproxy.observer;

/**
 * Created by Kenny Wang on 3/14/2016.
 */
public class SampleObserver implements Observer {
    private boolean started = false;

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        if (!started) {
            throw new RuntimeException(this.getClass() + " must be started");
        }
        started = false;
    }

}
