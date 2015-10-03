package br.com.santhyago.tests.listrepo.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import br.com.santhyago.tests.listrepo.data.GitHubContract.Repo;

/**
 * Created by san on 10/2/15.
 */
public class GitHubProvider extends ContentProvider {

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	private static final int REPO = 100;
	private static final int REPO_ID = 101;
	private GitHubDBHelper mDbHelper;

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = GitHubContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, GitHubContract.PATH_REPO, REPO);
		matcher.addURI(authority, GitHubContract.PATH_REPO + "/#", REPO_ID);

		return matcher;
	}

	@Override
	public boolean onCreate() {
		mDbHelper = new GitHubDBHelper(getContext());
		return true;
	}

	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		Cursor retCursor;
		switch (sUriMatcher.match(uri)) {
			// "repo/*"
			case REPO_ID: {
				retCursor = mDbHelper.getReadableDatabase().query(
						Repo.TABLE_NAME,
						projection,
						Repo._ID + " = '" + ContentUris.parseId(uri) + "'",
						null,
						null,
						null,
						sortOrder
				);
				break;
			}
			// "repo"
			case REPO: {
				retCursor = mDbHelper.getReadableDatabase().query(
						Repo.TABLE_NAME,
						projection,
						selection,
						selectionArgs,
						null,
						null,
						sortOrder
				);
				break;
			}

			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		if (getContext() != null)
			retCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return retCursor;
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		final int match = sUriMatcher.match(uri);

		switch (match) {
			case REPO:
				return Repo.CONTENT_TYPE;
			case REPO_ID:
				return Repo.CONTENT_ITEM_TYPE;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		Uri returnUri;

		switch (match) {
			case REPO: {
				long _id = db.insert(Repo.TABLE_NAME, null, values);
				if (_id > 0)
					returnUri = Repo.buildRepoUri(_id);
				else
					throw new android.database.SQLException("Failed to insert row into " + uri);
				break;
			}
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		if (getContext() != null)
			getContext().getContentResolver().notifyChange(uri, null);
		return returnUri;
	}

	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);

		int rowsDeleted;

		switch (match) {
			case REPO:
				rowsDeleted = db.delete(
						Repo.TABLE_NAME, selection, selectionArgs);
				break;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		if (selection == null || rowsDeleted != 0) {
			if (getContext() != null)
				getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowsDeleted;
	}

	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);

		int rowsUpdated;

		switch (match) {
			case REPO:
				rowsUpdated = db.update(Repo.TABLE_NAME, values, selection, selectionArgs);
				break;
			default:
				throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		if (rowsUpdated != 0) {
			if (getContext() != null)
				getContext().getContentResolver().notifyChange(uri, null);
		}
		return rowsUpdated;
	}

	@Override
	public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);

		switch (match) {
			case REPO:
				db.beginTransaction();

				int returnCount = 0;

				try {
					for (ContentValues value : values) {
						long _id = db.insert(Repo.TABLE_NAME, null, value);
						if (_id != -1) {
							returnCount++;
						}
					}
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
				if (getContext() != null)
					getContext().getContentResolver().notifyChange(uri, null);
				return returnCount;
			default:
				return super.bulkInsert(uri, values);
		}
	}

}
