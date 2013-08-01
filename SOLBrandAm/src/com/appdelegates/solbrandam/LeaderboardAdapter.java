package com.appdelegates.solbrandam;

import java.util.List;

import com.appdelegates.solnetwork.SOLEvent;

import android.content.Context;
import android.widget.ArrayAdapter;

public class LeaderboardAdapter extends ArrayAdapter<SOLEvent> {

	public LeaderboardAdapter(Context context, int resource,
			int textViewResourceId, List<SOLEvent> objects) {
		super(context, resource, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

}
