package io.sharif.pavilion.clientSide;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import io.sharif.pavilion.R;
import io.sharif.pavilion.model.ServerObj;

/**
 * Created by learnovate on 3/30/14.
 */
public class ConnectedServerFragment extends Fragment {

    final static String ARG_POSITION = "position";
//    private int currentPosition = -1;
    ServerObj currentServerObj=null;
    MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        if(savedInstanceState != null){
            //if we recreated this Fragment (for instance from a screen rotate)
            //restore the previous article selection by getting it here
//            currentPosition = savedInstanceState.getInt(ARG_POSITION);
            currentServerObj=ServerObj.getServerOjbFromJson(savedInstanceState.getString("serverObj"));

        }
        //inflate the view for this fragment
        View myFragmentView = inflater.inflate(R.layout.article_fragment,container,false);
        return myFragmentView;

    }

    public void updateArticleView(ServerObj serverObj){//TODO change this to update view
        View v = getView();
//        String[] data = Ipsum.Articles;
//        article.setText(data[position]);
//        currentPosition = position;
        currentServerObj=serverObj;
        LinearLayout containerOfContents= (LinearLayout) v.findViewById(R.id.container_of_contents);
        containerOfContents.removeAllViews();
        ContentsViewBuilder contentsViewBuilder=new ContentsViewBuilder(activity);
        View contentsView=contentsViewBuilder.getView(serverObj.getContentsObj());
        containerOfContents.addView(contentsView);

    }

    @Override
    public void onStart() {
        super.onStart();

        //During startup, we should check if there are arguments (data)
        //passed to this Fragment. We know the layout has already been
        //applied to the Fragment so we can safely call the method that
        //sets the article text

        Bundle args = getArguments();
        if(args != null){
            //set the article based on the argument passed in
            updateArticleView(ServerObj.getServerOjbFromJson(args.getString("serverObj")));

        }else if (currentServerObj!=null){
            //set the article based on the saved instance state defined during onCreateView
            updateArticleView(currentServerObj);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the current selection for later recreation of this Fragment
        outState.putString("serverObj", currentServerObj.getJson());



    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity= (MainActivity) getActivity();
    }
}
