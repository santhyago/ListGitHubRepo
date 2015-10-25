package br.com.santhyago.tests.listrepo;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ListRepoAdapter extends CursorAdapter {

	public static class ViewHolder {
		public final TextView repoName;
		public final TextView repoDescription;

		public ViewHolder(View view) {
			repoName = (TextView) view.findViewById(R.id.list_item_repo_name);
			repoDescription = (TextView) view.findViewById(R.id.list_item_repo_description);
		}
	}

	public ListRepoAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

		ViewHolder viewHolder = new ViewHolder(view);
		view.setTag(viewHolder);
		
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder) view.getTag();

		String repoName = cursor.getString(FragmentListRepo.COL_REPO_NAME);
		viewHolder.repoName.setText(repoName);

		String repoDesc = cursor.getString(FragmentListRepo.COL_REPO_DESC);
		viewHolder.repoDescription.setText(repoDesc);
	}
}
