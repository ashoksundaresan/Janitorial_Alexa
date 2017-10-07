package external.apiai.model;

import java.io.Serializable;

import external.apiai.api.AIServiceException;

public class AIError implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private final String message;
    @SuppressWarnings("unused")
	private final AIResponse aiResponse;

    private AIServiceException exception;

    public AIError(final String message) {
        aiResponse = null;

        this.message = message;
    }

    public AIError(final AIServiceException e) {
        aiResponse = e.getResponse();
        message = e.getMessage();
        exception = e;
    }

    public AIError(final AIResponse aiResponse) {
        this.aiResponse = aiResponse;

        if (aiResponse == null) {
            message = "API.AI service returns empty result";
        }
        else if (aiResponse.getStatus() != null) {
            message = aiResponse.getStatus().getErrorDetails();
        } else {
            message = "API.AI service returns error code with empty status";
        }
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (exception != null) {
            return exception.toString();
        } else {
            return message;
        }
    }
}
