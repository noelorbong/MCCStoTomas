package com.example.mccstotomas.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.mccstotomas.Database.SqlLiteHelper;
import com.example.mccstotomas.MainActivity;
import com.example.mccstotomas.Model.UserModel;
import com.example.mccstotomas.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FragmentDashboard extends Fragment {

    private TextView txt_ambulance;
    public Activity mainActivity;
    SqlLiteHelper db;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view =  inflater.inflate(R.layout.frame_dashboard,
                container, false);
        mainActivity = (MainActivity) getActivity();

        db = new SqlLiteHelper(mainActivity);
        db.open();
        UserModel userModel = db.getSpecificUser();


        List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < 3; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("listview_title", listviewTitle[i]);
            hm.put("listview_discription", listviewShortDescription[i]);
            hm.put("listview_image", Integer.toString(listviewImage[i]));
            aList.add(hm);
        }

        String[] from = {"listview_image", "listview_title", "listview_discription"};
        int[] to = {R.id.listview_image, R.id.listview_item_title, R.id.listview_item_short_description};

        SimpleAdapter simpleAdapter = new SimpleAdapter(mainActivity.getBaseContext(), aList, R.layout.listview_activity, from, to);
        ListView androidListView = (ListView) view.findViewById(R.id.list_view);
        androidListView.setAdapter(simpleAdapter);
        androidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String title =  listviewTitle[i];
                String description =  listviewShortDescription[i];

                final FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment fragment = new FragmentEmergency();

                Bundle bundle = new Bundle();
                bundle.putInt("id", i);
                bundle.putString("title",title);
                bundle.putString("description",description);
                fragment.setArguments(bundle);
                ft.replace(R.id.frameContact, fragment, "NewFragmentTag");
                ft.commit();
            }
        });

        return  view;
        //return inflater.inflate(R.layout.activity_insert_data, container, false);
    }

    String[] listviewTitle = new String[]{
            "Ambulance", "Fire Fighters", "Police",
    };


    int[] listviewImage = new int[]{
            R.drawable.ambulance_96, R.drawable.firefighter_96, R.drawable.police_96,
    };

    String[] listviewShortDescription = new String[]{
            "Request Number of Ambulace", "Request Number of Fire Fighters", "Request Number of Police"
    };
}
