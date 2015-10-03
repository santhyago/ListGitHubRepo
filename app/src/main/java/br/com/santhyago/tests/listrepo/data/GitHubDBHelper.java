package br.com.santhyago.tests.listrepo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import br.com.santhyago.tests.listrepo.data.GitHubContract.Repo;

/**
 * Created by san on 10/2/15.
 */
public class GitHubDBHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;

	public static final String DATABASE_NAME = "github_repo.db";

	public GitHubDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		final String SQL_CREATE_REPO_TABLE = "CREATE TABLE " + Repo.TABLE_NAME + " (" +
				Repo._ID + " INTEGER PRIMARY KEY," +
				Repo.COL_USER + " TEXT NOT NULL, " +
				Repo.COL_NAME + " TEXT NOT NULL, " +
				Repo.COL_DESC + " TEXT, " +
				Repo.COL_STAR_COUNT + " INTEGER NOT NULL, " +
				Repo.COL_WTCH_COUNT + " INTEGER NOT NULL, " +
				Repo.COL_LANG + " TEXT, " +
				"UNIQUE (" + Repo.COL_USER + ", " + Repo.COL_NAME + ") ON CONFLICT IGNORE"+
				" );";

		db.execSQL(SQL_CREATE_REPO_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + Repo.TABLE_NAME);
		onCreate(db);
	}
}
