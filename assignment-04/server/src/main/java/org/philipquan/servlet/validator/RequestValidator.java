package org.philipquan.servlet.validator;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class RequestValidator {

	/**
	 * The {@link HttpServletResponse} status code if {@code validate}
	 * returned false. If called when {@code validate} returned true, a
	 * {@link RuntimeException} will be thrown.
	 */
	protected Integer status = -1;
	protected String errorMessage = null;
	protected HashMap<String, Object> map = new HashMap<>();

	/**
	 * Validates the incoming {@link HttpServletRequest} sent by the client.
	 *
	 * <br><br>
	 *
	 * If the request is not valid, an {@link HttpServletResponse} status code
	 * is cached along with the error message. Additionally, the validator
	 * extracts any values from the request and stores it in a local map. You
	 * can call {@link RequestValidator#getParameter} with the required key(s)
	 * to get the value(s).
	 *
	 * @param request the {@link HttpServletRequest} sent by the client
	 * @return true if the request's package is valid
	 * @throws ServletException
	 * @throws IOException
	 */
	public abstract Boolean validate(HttpServletRequest request) throws IOException, ServletException;

	/**
	 * @return the {@link HttpServletResponse} status code generated from
	 * {@code validate} returning false.
	 * @throws RuntimeException if {@code validate} returned {@code true}
	 */
	public Integer getStatus() {
		if (this.status.equals(-1)) {
			throw new RuntimeException("calling getStatus on RequestValidator that is valid.");
		}
		return this.status;
	}

	/**
	 * @return the error message generated from {@code validate} returning false.
	 * @throws RuntimeException if {@code validate} returned {@code true}
	 */
	public String getErrorMessage() {
		if (this.errorMessage == null) {
			throw new RuntimeException("calling getErrorMessage on RequestValidator that is valid.");
		}
		return this.errorMessage;
	}

	/**
	 * @param key the key of the value to return
	 * @return the value of the key provided
	 */
	public Object getParameter(String key) {
		return this.map.get(key);
	}
}