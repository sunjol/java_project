package protect.budgetwatch;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

public class BudgetActivity extends AppCompatActivity
{
    private final static String TAG = "BudgetWatch";

    private DBHelper _db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.budget_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        _db = new DBHelper(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final ListView budgetList = (ListView) findViewById(R.id.list);
        final TextView helpText = (TextView)findViewById(R.id.helpText);

        if(_db.getBudgetCount() > 0)
        {
            budgetList.setVisibility(View.VISIBLE);
            helpText.setVisibility(View.GONE);
        }
        else
        {
            budgetList.setVisibility(View.GONE);
            helpText.setVisibility(View.VISIBLE);
            helpText.setText(R.string.noBudgets);
        }



        final Bundle b = getIntent().getExtras();
        final long budgetStartMs = b != null ? b.getLong("budgetStart", 0) : 0;
        final long budgetEndMs = b != null ? b.getLong("budgetEnd", 0) : 0;




        final List<Budget> budgets = _db.getBudgets(0, budgetEndMs);
        final BudgetAdapter budgetListAdapter = new BudgetAdapter(this, budgets);
        budgetList.setAdapter(budgetListAdapter);

        registerForContextMenu(budgetList);

        budgetList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Budget budget = (Budget)parent.getItemAtPosition(position);
                if(budget == null)
                {
                    Log.w(TAG, "Clicked budget at position " + position + " is null");
                    return;
                }

                Intent i = new Intent(getApplicationContext(), TransactionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("budget", budget.name);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        Budget blankBudget = _db.getBlankBudget(budgetStartMs, budgetEndMs);

        setupTotalEntry(budgets, blankBudget);
    }

    private void setupTotalEntry(final List<Budget> budgets, final Budget blankBudget)
    {
        final TextView budgetName = (TextView)findViewById(R.id.budgetName);
        final TextView budgetValue = (TextView)findViewById(R.id.budgetValue);
        final ProgressBar budgetBar = (ProgressBar)findViewById(R.id.budgetBar);

        budgetName.setText(R.string.totalBudgetTitle);

        int max = 0;
        int current = 0;

        for(Budget budget : budgets)
        {
            max += budget.max;
            current += budget.current;
        }

        current += blankBudget.current;

        budgetBar.setMax(max);
        budgetBar.setProgress(current);

        String fraction = String.format(getResources().getString(R.string.fraction), current, max);
        budgetValue.setText(fraction);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list)
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.view_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView listView = (ListView) findViewById(R.id.list);

        if(info != null)
        {
            Budget budget = (Budget) listView.getItemAtPosition(info.position);

            if (budget != null && item.getItemId() == R.id.action_edit)
            {
                Intent i = new Intent(getApplicationContext(), BudgetViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id", budget.name);
                bundle.putBoolean("view", true);
                i.putExtras(bundle);
                startActivity(i);

                return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.budget_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_add)
        {
            Intent i = new Intent(getApplicationContext(), BudgetViewActivity.class);
            startActivity(i);
            return true;
        }





        if(id == android.R.id.home)
        {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        _db.close();
        super.onDestroy();
    }
}