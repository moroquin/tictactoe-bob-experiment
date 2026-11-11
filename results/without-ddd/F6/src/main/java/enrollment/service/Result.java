package enrollment.service;

/**
 * The result of any service operation.
 * ok==true means the operation succeeded; message carries the response text.
 */
public record Result(boolean ok, String message) {
    public static Result ok(String msg) { return new Result(true, msg); }
    public static Result error(String msg) { return new Result(false, msg); }
}
