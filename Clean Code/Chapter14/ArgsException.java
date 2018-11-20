import static com. objectmentor. utilities. args. ArgsException. ErrorCode.*; 

public class ArgsException extends Exception {
    private char errorArgumentId = '\0';
    private String errorParameter = null;
    private ErrorCode errorCode = OK;

    public ArgsException() {}
    public ArgsException(String message) {
        super( message);
    }
    public ArgsException( ErrorCode errorCode) {
        this. errorCode = errorCode; 　
    }
    public ArgsException( ErrorCode errorCode, String errorParameter) {
        this. errorCode = errorCode;
        this. errorParameter = errorParameter; 　
    }
    public ArgsException( ErrorCode errorCode, char errorArgumentId, String errorParameter) {
        this. errorCode = errorCode;
        this. errorParameter = errorParameter;
        this. errorArgumentId = errorArgumentId; 　
    }

    public char getErrorArgumentId() {
        return errorArgumentId; 　
    }
    public void setErrorArgumentId( char errorArgumentId) {
        this. errorArgumentId = errorArgumentId; 　
    }
    public String getErrorParameter() {
        return errorParameter; 　
    }
    public void setErrorParameter( String errorParameter) {
        this. errorParameter = errorParameter; 　
    }
    public ErrorCode getErrorCode() {
        return errorCode; 　
    }
    public void setErrorCode( ErrorCode errorCode) {　
        this. errorCode = errorCode; 　
    }
    public String errorMessage() {
        switch (errorCode) {
            case OK:　
                return "TILT:Shouldnotgethere.";
            case UNEXPECTED_ ARGUMENT:　
                return String. format(" Argument 　-% cunexpected.", errorArgumentId);
            case MISSING_ STRING:　
                return String. format(" Could not find string parameter for -%c.", errorArgumentId); 
            case INVALID_ INTEGER:　
                return String. format(" Argument 　-% cexpectsanintegerbutwas 　'% s'.", errorArgumentId, errorParameter);
            case MISSING_ INTEGER:　
                return String. format(" Could not find integer parameter for -%c.", errorArgumentId);
            case INVALID_ DOUBLE: 
                return String. format(" Argument -%c expects a double but was '%s'.", errorArgumentId, errorParameter);
            case MISSING_ DOUBLE:　
                return String. format(" Could not find double parameter for -%c.", errorArgumentId);
            case INVALID_ ARGUMENT_ NAME:　
                return String. format("'% c' is nota valid argument name.",
            case INVALID_ ARGUMENT_ FORMAT:　
                return String. format("'% s' is not a valid argument format.", errorParameter);　
        }
        return ""; 　
    }
    public enum ErrorCode {
        OK, INVALID_ ARGUMENT_ FORMAT, UNEXPECTED_ ARGUMENT, INVALID_ ARGUMENT_ NAME, MISSING_ STRING, MISSING_ INTEGER, INVALID_ INTEGER, MISSING_ DOUBLE, INVALID_ double 　
    }
}