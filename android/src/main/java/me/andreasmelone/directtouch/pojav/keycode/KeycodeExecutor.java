package me.andreasmelone.directtouch.pojav.keycode;

import android.view.KeyEvent;

public interface KeycodeExecutor {
    /// inits some state required for exec to work
    boolean init();
    void exec(KeyEvent event);

    String getName();

    class NoopFallback implements KeycodeExecutor {
        @Override
        public boolean init() {
            return true;
        }

        @Override
        public void exec(KeyEvent event) {

        }

        @Override
        public String getName() {
            return "NOOP Fallback";
        }
    }
}
