package org.philipquan.servlet;

import static org.philipquan.servlet.utility.ServletResponseHelper.setResponse;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.philipquan.dal.MQConnectionManager;
import org.philipquan.servlet.validator.PostReviewValidator;
import org.philipquan.servlet.validator.RequestValidator;

@WebServlet("/reviews/*")
public class ReviewServlet extends HttpServlet {

	private MQConnectionManager mbManager;

	public void init() {
		this.mbManager = new MQConnectionManager();
	}

	@Override
	public void destroy() {
		super.destroy();
		this.mbManager.close();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		Map<String, Object> messages = new HashMap<>();

		RequestValidator validator = new PostReviewValidator();
		if (!validator.validate(request)) {
			setResponse(messages, response, validator.getStatus(), validator.getErrorMessage());
			return;
		}

		String action = (String) validator.getParameter("action");
		String albumId = (String) validator.getParameter("albumId");
		int likes = 0;
		int dislikes = 0;
		if (action.equals("like")) {
			likes = 1;
		} else {
			dislikes = 1;
		}

		try {
			Channel channel = this.mbManager.getChannel();
			String exchange = "";
			BasicProperties properties = null;
			byte[] body = String.format("{\"albumId\":%s, \"likes\":%d, \"dislikes\":%d}", albumId, likes, dislikes).getBytes();

			channel.basicPublish(exchange, MQConnectionManager.REACTION_QUEUE, properties, body);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}
		setResponse(messages, response, HttpServletResponse.SC_CREATED, "write successful");
	}
}
