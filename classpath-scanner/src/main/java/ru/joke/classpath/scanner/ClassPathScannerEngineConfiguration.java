package ru.joke.classpath.scanner;

public record ClassPathScannerEngineConfiguration(boolean stateful) {

    public static ClassPathScannerEngineConfiguration defaultConfig() {
        return new ClassPathScannerEngineConfiguration(false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean stateful;

        public void stateful() {
            this.stateful = true;
        }

        public void stateless() {
            this.stateful = false;
        }

        public ClassPathScannerEngineConfiguration build() {
            return new ClassPathScannerEngineConfiguration(this.stateful);
        }
    }
}
