package com.appdelegates.solbrandam;

import java.util.ArrayList;

import com.appdelegates.solnetwork.Gamer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GamerAdapter extends ArrayAdapter<Gamer> {

	Context context; 
	int layoutResourceId;    
	ArrayList<Gamer> data = null;
	    
	public GamerAdapter(Context context, int layoutResourceId, ArrayList<Gamer> data) {
		super(context, layoutResourceId, data);
	    this.layoutResourceId = layoutResourceId;
	    this.context = context;
	    this.data = data;
    }

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
	    GamerHolder holder = null;
	        
	    if(row == null){
	    	LayoutInflater inflater = LayoutInflater.from(context);
	        row = inflater.inflate(layoutResourceId, parent, false);
	            
	        holder = new GamerHolder();
	        holder.gamertagTV = (TextView)row.findViewById(R.id.textViewGamertag);
	        holder.scoreTV = (TextView)row.findViewById(R.id.textViewScore);
	        holder.emailTV = (TextView)row.findViewById(R.id.textViewEmail);
	       
	            
	        row.setTag(holder);
	    } else {
	            holder = (GamerHolder)row.getTag();
	        }
	        
	        Gamer gamer = data.get(position);
	        holder.gamertagTV.setText(gamer.getGamertag());
	        holder.scoreTV.setText(""+gamer.getScore());
	        holder.emailTV.setText(gamer.getEmail());
	        
	        
	        return row;
	    }
	    
	    static class GamerHolder
	    {
	       TextView gamertagTV;
	       TextView scoreTV;
	       TextView emailTV;
	      
	    }
	}
	
