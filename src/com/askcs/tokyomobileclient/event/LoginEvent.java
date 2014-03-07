package com.askcs.tokyomobileclient.event;

public class LoginEvent {

    private boolean successful;

    /**
     * @param successful
     */
    public LoginEvent(boolean successful) {
        super();
        this.successful = successful;
    }

    /**
     * @return the successful
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * @param successful
     *            the successful to set
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

}
