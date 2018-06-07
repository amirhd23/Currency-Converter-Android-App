package com.comp3617.currencyconverter;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.comp3617.currencyconverter.model.ExchangeRate;
import com.comp3617.currencyconverter.network.DataManager;

public class MainActivity extends SingleFragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CurrencyListFragment.Callbacks,
        RateAlertFragment.Callbacks{

    @Override
    protected Fragment createFragment() {
        return new CurrencyListFragment();
    }

    @Override
    public void onExchangeRateSelected(ExchangeRate exchangeRate) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ConversionFragment.newInstance(exchangeRate))
                .addToBackStack(null)
                .commit();
        setTitle(getString(R.string.app_name));
    }

    public void onSettingsRequested() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .addToBackStack(null).commit();
        setTitle(getString(R.string.settings_title));
    }

    public void onSetUpAlertRequested() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RateAlertFragment())
                .addToBackStack(null)
                .commit();
        setTitle(getString(R.string.set_up_rate_alert));
    }

    @Override
    public void onCancelRateAlertRequested() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CurrencyListFragment())
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_settings) {
            onSettingsRequested();
        } else if (id == R.id.menu_setup_rate_alert) {
            onSetUpAlertRequested();
        }  else if (id == R.id.menu_share_app) {
            createShareIntent();
        } else if (id == R.id.menu_show_credit) {
            showCreditDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createShareIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_text));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_subject));
        intent.setType("text/plain");
        intent.createChooser(intent, getString(R.string.send_to));
        startActivity(intent);
    }

    private void showCreditDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.credit_dialog_title))
                .setMessage(getResources().getString(R.string.credit_dialog_text))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
