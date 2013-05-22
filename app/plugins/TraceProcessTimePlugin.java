package plugins;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.jpa.JPA;
import play.mvc.Http.Header;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.templates.JavaExtensions;

import com.jamonapi.MonitorFactory;

public class TraceProcessTimePlugin extends PlayPlugin {
	private static ThreadLocal<Long> databaseQueryCount = new ThreadLocal<Long>();
	private static ThreadLocal<Long> hibernateQueryCount = new ThreadLocal<Long>();
	private static ThreadLocal<Long> collectionFetchCount = new ThreadLocal<Long>();
	private static ThreadLocal<Long> entityFetchCount = new ThreadLocal<Long>();
	private static ThreadLocal<Long> time = new ThreadLocal<Long>();
	private static ThreadLocal<Statistics> statistics = new ThreadLocal<Statistics>();
	private static boolean switchTag = false;

	@Override
	public void onLoad() {
		Logger.info("TraceProcessTimePlugin start up.");
		this.switchTag = "on".equals(Play.configuration
				.getProperty("TraceProcessTimePlugin.tag"));
	}

	@Override
	public void beforeActionInvocation(Method actionMethod) {
		if (switchTag) {
			Statistics stats = ((Session) JPA.em().getDelegate())
					.getSessionFactory().getStatistics();
			stats.setStatisticsEnabled(true);
			statistics.set(stats);

			databaseQueryCount.set(currentDatabaseQueryCount());
			hibernateQueryCount.set(currentHibernateQueryCount());
			collectionFetchCount.set(currentCollectionFetchCount());
			entityFetchCount.set(currentEntityFetchCount());
			time.set(System.currentTimeMillis());
		}
	}

	@Override
	public void afterActionInvocation() {
		if (switchTag) {
			long realtime = System.currentTimeMillis() - time.get();
			write(Request.current().action, "DatabaseQueryCount : "
					+ getDatabaseQueryCount() + "\nHibernateQueryCount : "
					+ getHibernateQueryCount() + "\nCollectionFetchCount : "
					+ getCollectionFetchCount() + "\nEntityFetchCount : "
					+ getEntityFetchCount(), false);
			Set<String> excludeActions = new HashSet<String>() {
				{
					add("Application.download");
					add("Application.getPageTitle");
					add("Application.upload");
					add("DojoController.uploadBagdeFile");
					add("GroupController.uploadPic");
					add("ProfileController.uploadAvatarFile");
				}
			};
			if (excludeActions.contains(Request.current().action)) {

			} else if (realtime > 1000) {
				write("timeTook-Blocker", Request.current().action + " took "
						+ realtime + ". \n", true);
			} else if (realtime > 500) {
				write("timeTook-Critical", Request.current().action + " took "
						+ realtime + ". \n", true);
			} else if (realtime > 300) {
				write("timeTook-Major", Request.current().action + " took "
						+ realtime + ". \n", true);
			} else if (realtime > 200) {
				write("timeTook-Minor", Request.current().action + " took "
						+ realtime + "ms. \n", true);
			} else {
				write("timeTook-Trivial", Request.current().action + " took "
						+ realtime + ". \n", true);
			}
			if (Request.current().headers.get("tracetime") != null
					&& "on".equals(Request.current().headers.get("tracetime")
							.value())) {
				Response.current().headers.put("traceTime", new Header(
						"traceTime", realtime + ""));
			}
		}
	}

	@Override
	public void onApplicationStop() {
		StringBuffer sb = new StringBuffer(
				"||ACTION||HITS||AVG||STDDEV||MIN||MAX||");
		for (Object[] obj : MonitorFactory.getRootMonitor().getBasicData()) {
			if (((String) obj[0]).matches("[a-zA-Z0-9-_\\.]+\\(\\), ms\\.")) {
				sb.append('\n').append('|')
						.append(((String) obj[0]).replace(", ms.", ""))
						.append('|').append(obj[1]).append('|').append(obj[2])
						.append('|').append(obj[4]).append('|').append(obj[6])
						.append('|').append(obj[7]).append('|');
			}
		}
		write("all-" + JavaExtensions.format(new Date(), "yyyyMMdd"),
				sb.toString(), true);
		super.onApplicationStop();
	}

	private static long getDatabaseQueryCount() {
		return currentDatabaseQueryCount() - databaseQueryCount.get();
	}

	private static long getHibernateQueryCount() {
		return currentHibernateQueryCount() - hibernateQueryCount.get();
	}

	private static long getCollectionFetchCount() {
		return currentCollectionFetchCount() - collectionFetchCount.get();
	}

	private static long getEntityFetchCount() {
		return currentEntityFetchCount() - entityFetchCount.get();
	}

	private static long currentDatabaseQueryCount() {
		return statistics.get().getPrepareStatementCount();
	}

	private static long currentHibernateQueryCount() {
		return statistics.get().getQueryExecutionCount();
	}

	private static long currentCollectionFetchCount() {
		return statistics.get().getCollectionFetchCount();
	}

	private static long currentEntityFetchCount() {
		return statistics.get().getEntityFetchCount();
	}

	private void write(String fileName, String content, boolean append) {
		FileWriter fw = null;
		try {
			File folder = new File(Play.applicationPath.getAbsolutePath()
					+ "/logs/status");
			FileUtils.forceMkdir(folder);
			File file = new File(folder.getAbsolutePath() + "/" + fileName
					+ ".log");
			fw = new FileWriter(file, append);
			fw.append(content).append("\r\n");
		} catch (IOException e) {
			Logger.error(e, e.getMessage());
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					Logger.error(e, e.getMessage());
				}
			}
		}
	}
}
