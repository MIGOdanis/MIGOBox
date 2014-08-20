package com.example.kurumap.tools;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class Index extends Fragment {
		
	    public static Index newInstance(int num) {
	    	Index fragment = new Index();
	        // Supply num input as an argument.
	        Bundle args = new Bundle();
	        args.putInt("num", num);
	        fragment.setArguments(args);
	        return fragment;
	    }
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	    }
	    /**为Fragment加载布局时调用**/
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	                                                                                                                                                                                                                                                                                                                      
	        View view = inflater.inflate(R.layout.activity_index, null);
	        TextView tv = (TextView) view.findViewById(R.id.title);
	        tv.setText("ibdex");
	        return view;
	    }
}
