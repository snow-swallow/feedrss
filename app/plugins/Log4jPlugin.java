package plugins;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;

import play.Play;
import play.Play.Mode;
import play.PlayPlugin;

public class Log4jPlugin extends PlayPlugin {
	@Override
	public void onApplicationStart() {
		Logger logger = Logger.getRootLogger();
		SMTPAppender smtpAppender = new SMTPAppender(
				new TriggeringEventEvaluator() {
					@Override
					public boolean isTriggeringEvent(LoggingEvent event) {
						String message = event.getRenderedMessage();
						if (message.contains("action not found")
								|| message
										.contains("The email has not been sent")) {
							return false;
						}
						return event.getLevel().isGreaterOrEqual(Level.ERROR)
								&& Play.mode == Mode.PROD
								&& "on".equals(Play.configuration.getProperty(
										"application.log.custom.out", "off"));
					}
				});
		smtpAppender.setBufferSize(512);
		smtpAppender.setFrom(Play.configuration.getProperty(
				"mail.sender.address", "aiban@deach.net"));
		smtpAppender.setLayout(new PlayPatternLayout(
				"%-d{yyyy-MM-dd HH:mm:ss} [%p] - [%l]  %m%n"));
		smtpAppender.setName("MAIL");
		smtpAppender.setSMTPHost(Play.configuration.getProperty(
				"mail.smtp.host", "deach.net"));
		smtpAppender.setSMTPPort(Integer.parseInt(Play.configuration
				.getProperty("mail.smtp.port", "6025")));
		smtpAppender.setSMTPPassword(Play.configuration.getProperty(
				"mail.smtp.pass", "aiban"));
		smtpAppender.setSMTPUsername(Play.configuration.getProperty(
				"mail.smtp.user", "aiban"));
		smtpAppender.setSubject("Server Error ( port : "
				+ Play.configuration.getProperty("http.port")
				+ " ; applicationPath : " + Play.applicationPath + " ; ID : "
				+ Play.id + " )");
		smtpAppender.setThreshold(Level.ERROR);
		smtpAppender
				.setTo("inguochenfan@163.com,reiz6153@hotmail.com,zhao.xu@diqu.com.cn");
		smtpAppender.activateOptions();
		logger.addAppender(smtpAppender);
	}

	class PlayPatternLayout extends PatternLayout {

		public PlayPatternLayout(String pattern) {
			super(pattern);
		}

		@Override
		public String getContentType() {
			return "text/plain;charset=utf-8";
		}
	}
}