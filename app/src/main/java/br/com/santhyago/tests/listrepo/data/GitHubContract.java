package br.com.santhyago.tests.listrepo.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.List;

/**
 * Created by san on 10/2/15.
 */
public class GitHubContract {
	public static final String CONTENT_AUTHORITY = "br.com.santhyago.tests.listrepo";

	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	public static final String PATH_REPO = "repo";

	public static final class Repo implements BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPO).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_REPO;
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_REPO;

		public static final String TABLE_NAME = "repo";
		public static final String COL_USER = "user";
		public static final String COL_NAME = "name";
		public static final String COL_DESC = "description";
		public static final String COL_STAR_COUNT = "stargazers_count";
		public static final String COL_WTCH_COUNT = "watchers_count";
		public static final String COL_LANG = "language";

		public static Uri buildRepoUri(long id) {
			return ContentUris.withAppendedId(CONTENT_URI, id);
		}

		public static ContentValues[] buildContentValues(List<br.com.santhyago.tests.listrepo.model.Repo> repos) {
			int listRepoSize = repos.size();
			ContentValues[] values = new ContentValues[listRepoSize];
			ContentValues value;
			for (int i = 0; i < listRepoSize; i++) {
				value = new ContentValues();
				value.put(COL_USER, repos.get(i).getOwner().getLogin());
				value.put(COL_NAME, repos.get(i).getName());
				value.put(COL_DESC, repos.get(i).getDescription());
				value.put(COL_STAR_COUNT, repos.get(i).getStargazersCount());
				value.put(COL_WTCH_COUNT, repos.get(i).getWatchersCount());
				value.put(COL_LANG, repos.get(i).getLanguage());
				values[i] = value;
			}

			return values;
		}
	}

}
