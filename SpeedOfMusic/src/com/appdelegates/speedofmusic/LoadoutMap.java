package com.appdelegates.speedofmusic;

import java.util.HashMap;

public class LoadoutMap {
	

	HashMap<String, Object> genreMap;
	
	public LoadoutMap(){
		
		genreMap = new HashMap< String, Object >();
		
		// Rock Genre
		HashMap <String, Object> rockSubMap = new HashMap<String, Object>();
		rockSubMap.put("tempo", 160);
		rockSubMap.put("base beat", R.raw.basebeat);
		rockSubMap.put("left1", R.raw.musicaleffect1);
		rockSubMap.put("left2", R.raw.musicaleffect2);
		rockSubMap.put("left3", R.raw.musicaleffect3);
		rockSubMap.put("left4", R.raw.musicaleffect4);
		rockSubMap.put("left5", R.raw.musicaleffect5);
		genreMap.put("rock", rockSubMap);
		
		// Country Genre
		HashMap<String, Object> countrySubMap = new HashMap<String, Object>();
		countrySubMap.put("tempo", 90);
		countrySubMap.put("base beat", R.raw.basebeat);
		countrySubMap.put("left1", R.raw.musicaleffect1);
		countrySubMap.put("left2", R.raw.musicaleffect2);
		countrySubMap.put("left3", R.raw.musicaleffect3);
		countrySubMap.put("left4", R.raw.musicaleffect4);
		countrySubMap.put("left5", R.raw.musicaleffect5);
		genreMap.put("country", countrySubMap);
		
		// DEFAULT (ERROR) Type
		HashMap<String, Object> errSubMap = new HashMap<String, Object>();
		errSubMap.put("header", "Error");
		errSubMap.put("title", "Error, Unimplemented");
		genreMap.put("oops", errSubMap);
		
	}
	
	@SuppressWarnings("unchecked")
	public HashMap< String, Object> getMapForType(String type){
		
		HashMap< String, Object > rval = (HashMap<String, Object>) genreMap.get(type);
		
		if (rval==null)
			rval = (HashMap<String, Object>) genreMap.get("oops");
		
		return rval;
	}

}
