package io.sharif.pavilion.clientSide;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

import io.sharif.pavilion.R;
import io.sharif.pavilion.activities.HelpActivity;
import io.sharif.pavilion.activities.SendProblemActivity;
import io.sharif.pavilion.activities.SettingsActivity;
import io.sharif.pavilion.model.ServerObj;
import io.sharif.pavilion.network.DataStructures.ApInfo;
import io.sharif.pavilion.serverSide.ServerActivity;

public class ClientActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ServersListFragment.OnHeadlineSelectedListener{
    public  static Context context;
    CustomClientService clientService;
    ApInfo connectedServerApInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_client);
        context=this.getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clientService!=null){
                    clientService.leave();
                }
                clientService.scan();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);





        //check if the activity is using the layout version
        //with the FrameLayout.  if so, we have to add the fragment
        //(it wont be done automatically )
        if(findViewById(R.id.container) != null){

            //However if were being restored from a previous state,
            //then dont do anything
            if(savedInstanceState != null){
                return;
            }

            //Crate an instance of the Headline Fragment
            ServersListFragment serversListFragment = new ServersListFragment();

//            ServersListAdapter myAdapterC=new ServersListAdapter(getApplicationContext(), R.layout.row_for_servers_list , null);
//            serversListFragment.setListAdapter(myAdapterC);

            //In the case this activity was started with special instructions from an Intent,
            //pass the Intent's extras to the fragment as arguments
            serversListFragment.setArguments(getIntent().getExtras());

            //Ask the Fragment manager to add it to the FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.container, serversListFragment)
                    .commit();


        }

    }
    @Override
    public void onServerSelected(ServerObj serverObj) {
        //Capture the article fragment from the activity's dual-pane layout
        ConnectedServerFragment connectedServerFragment = (ConnectedServerFragment) getFragmentManager().findFragmentById(R.id.article_fragment);

        //if we dont find one, we must not be in two pane mode
        //lets swap the Fragments instead
        if(connectedServerFragment != null){

            //we must be in two pane layout
            connectedServerFragment.updateArticleView(serverObj);

        }else{
            //we must be in one pane layout

            //Create Fragment and give it an arguement for the selected article right away
            ConnectedServerFragment swapFragment = new ConnectedServerFragment();
            Bundle args = new Bundle();
               args.putString("serverObj",serverObj.getJson());
            swapFragment.setArguments(args);

            //now that the Fragment is prepared, swap it

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, swapFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
@Override
public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else if(getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.start_server) {
            clientService.stop();
            Intent myIntent = new Intent(ClientActivity.this,ServerActivity.class);
            startActivity(myIntent);
        } else if (id == R.id.nav_send_problem) {
            Intent myIntent = new Intent(ClientActivity.this,SendProblemActivity.class);
            startActivity(myIntent);
        } else if (id == R.id.nav_send_app) {

            // Get current ApplicationInfo to find .apk path
            ApplicationInfo app = getApplicationContext().getApplicationInfo();
            String filePath = app.sourceDir;
            Intent intent = new Intent(Intent.ACTION_SEND);
            // MIME of .apk is "application/vnd.android.package-archive".
            // but Bluetooth does not accept this. Let's use "*/*" instead.
            intent.setType("*/*");
            // Only use Bluetooth to send .apk
            // intent.setPackage("com.android.bluetooth");
            // Append file and send Intent
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(filePath)));
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.ic_menu_send_app)));

        } else if (id == R.id.nav_guide) {
            Intent myIntent = new Intent(ClientActivity.this,HelpActivity.class);
            startActivity(myIntent);

        }  else if (id == R.id.nav_settings) {
            Intent myIntent = new Intent(ClientActivity.this,SettingsActivity.class);
            startActivity(myIntent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
