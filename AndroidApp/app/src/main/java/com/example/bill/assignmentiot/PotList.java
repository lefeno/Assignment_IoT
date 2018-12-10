package com.example.bill.assignmentiot;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class PotList extends ArrayAdapter<Param> {

    private Activity context;
    private List<Param> Pots;

    public PotList(Activity context, List<Param> Pots){
        super(context, R.layout.list_layout, Pots); //ArrayAdapter constructor
        this.context = context;
        this.Pots = Pots;
    }

    @NonNull //only this view
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Param pot = Pots.get(position);
        // Inflate the view
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.list_layout, null, true);
        // Lookup view for data population
        TextView textViewValue = (TextView) listViewItem.findViewById(R.id.textHumid);
        TextView textViewPot = (TextView) listViewItem.findViewById(R.id.textPot);
        TextView textViewStatus = (TextView) listViewItem.findViewById(R.id.textStatus);
        // Populate the data into the template view susing the data object
        textViewPot.setText(pot.getName());
        if (pot.isAuto()){
            textViewStatus.setText("Auto-water: True");
        }
        else{
            textViewStatus.setText("Auto-water: False");
        }
        textViewValue.setText(pot.getValue());
        //Return the completed view to render on screen
        return listViewItem;
    }

}
