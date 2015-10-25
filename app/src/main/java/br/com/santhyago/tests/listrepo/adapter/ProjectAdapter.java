package br.com.santhyago.tests.listrepo.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.santhyago.tests.listrepo.FragmentListRepo;
import br.com.santhyago.tests.listrepo.R;

/**
 * Created by san on 10/24/15.
 * Project ListRepo
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
	private final Context context;
	private Cursor cursor;
	private final ProjectOnClickListener onClickListener;

	public ProjectAdapter(Context context, Cursor cursor, ProjectOnClickListener onClickListener) {
		this.context = context;
		this.cursor = cursor;
		this.onClickListener = onClickListener;
	}

	@Override
	public ProjectAdapter.ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
		return new ProjectViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final ProjectAdapter.ProjectViewHolder holder, final int position) {
		if (cursor != null) {
			cursor.moveToPosition(position);

			String repoName = cursor.getString(FragmentListRepo.COL_REPO_NAME);
			holder.repoName.setText(repoName);

			String repoDesc = cursor.getString(FragmentListRepo.COL_REPO_DESC);
			holder.repoDescription.setText(repoDesc);

			if (onClickListener != null) {
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickListener.onClickProject(holder.itemView, position);
					}
				});
			}
		}
	}

	@Override
	public int getItemCount() {
		return (cursor != null ? cursor.getCount() : 0);
	}

	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	public Cursor swapCursor(Cursor cursor) {
		if (this.cursor == cursor) {
			return null;
		}
		Cursor oldCursor = cursor;
		this.cursor = cursor;
		if (cursor != null) {
			this.notifyDataSetChanged();
		}
		return oldCursor;
	}

	private Object getItem(int position) {
		cursor.moveToPosition(position);
		return cursor;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public interface ProjectOnClickListener {
		void onClickProject(View view, int idx);
	}

	public static class ProjectViewHolder extends RecyclerView.ViewHolder {
		public final TextView repoName;
		public final TextView repoDescription;

		public ProjectViewHolder(View itemView) {
			super(itemView);
			repoName = (TextView) itemView.findViewById(R.id.list_item_repo_name);
			repoDescription = (TextView) itemView.findViewById(R.id.list_item_repo_description);
		}
	}
}
