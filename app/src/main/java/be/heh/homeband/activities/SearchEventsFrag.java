package be.heh.homeband.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import be.heh.homeband.R;
import be.heh.homeband.activities.searchevents.SearchEventsResultActivity;
import be.heh.homeband.app.HomebandApiInterface;
import be.heh.homeband.app.HomebandApiReponse;
import be.heh.homeband.app.HomebandGPSTracker;
import be.heh.homeband.app.HomebandRetrofit;
import be.heh.homeband.entities.Evenement;
import be.heh.homeband.entities.Groupe;
import be.heh.homeband.entities.Style;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchEventsFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchEventsFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchEventsFrag extends Fragment implements View.OnClickListener {
    ArrayAdapter<Style> adapterStyle;
    Spinner spinStyle;

    Button btnRecherche;
    ImageButton btnLocalisationEvents;

    EditText etKilometre;
    EditText etDu;
    EditText etAu;
    EditText etAdresse;

    TextView tvDateDu;
    TextView tvDateAu;

    Switch swAfficherDate;

    Calendar calendarDateDu;
    Calendar calendarDateAu;

    DatePickerDialog pickerDateDu;
    DatePickerDialog pickerDateAu;

    SimpleDateFormat dateFormatter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SearchEventsFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchEventsFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchEventsFrag newInstance(String param1, String param2) {
        SearchEventsFrag fragment = new SearchEventsFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myview = inflater.inflate(R.layout.fragment_search_events, container, false);
        initialisation(myview);
        initStyles();
        return myview;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onClick(View v) {
        if(v == btnRecherche){
            getEvents();
        } else if (v == etDu) {
            pickerDateDu.show();
        } else if (v == etAu) {
            pickerDateAu.show();
        }
        else if (v == btnLocalisationEvents){
            getLocalisations();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    public void initialisation(View myview){
        // Bouton rechercher
        btnRecherche = (Button) myview.findViewById(R.id.btnRechercheEvents);
        btnRecherche.setOnClickListener(this);
        btnLocalisationEvents = (ImageButton) myview.findViewById((R.id.btnLocalisationEvents)) ;
        btnLocalisationEvents.setOnClickListener(this);
        // Spinner Style
        spinStyle = (Spinner) myview.findViewById(R.id.spinStyle);

        // Localisation
        etKilometre = (EditText) myview.findViewById(R.id.etKilometre);

        // Dates
        etDu = (EditText)  myview.findViewById(R.id.etAu);
        etAu = (EditText)  myview.findViewById(R.id.etDu);
        etAdresse = (EditText)  myview.findViewById(R.id.etAdresse);
        tvDateDu = (TextView) myview.findViewById(R.id.tvDu);
        tvDateAu = (TextView) myview.findViewById(R.id.tvAu);
        swAfficherDate = (Switch) myview.findViewById(R.id.swDate);


        // Switch Affichage Date
        swAfficherDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                etDu.setVisibility(View.VISIBLE);
                etAu.setVisibility(View.VISIBLE);
                tvDateDu.setVisibility(View.VISIBLE);
                tvDateAu.setVisibility(View.VISIBLE);
            } else {
                etDu.setVisibility(View.INVISIBLE);
                etAu.setVisibility(View.INVISIBLE);
                tvDateDu.setVisibility(View.INVISIBLE);
                tvDateAu.setVisibility(View.INVISIBLE);
            }
            }
        });

        initDate();
    }

    public void initStyles(){
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(HomebandRetrofit.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Création d'une instance du service avec Retrofit
            HomebandApiInterface serviceApi = retrofit.create(HomebandApiInterface.class);

            // Requête vers l'API
            serviceApi.getStyles().enqueue(new Callback<HomebandApiReponse>() {
                @Override
                public void onResponse(Call<HomebandApiReponse> call, Response<HomebandApiReponse> response) {

                    // En fonction du code HTTP de Retour (2** = Successful)
                    if (response.isSuccessful()) {

                        // Récupération de la réponse de l'API
                        HomebandApiReponse res = response.body();
                        res.mapResultat();

                        CharSequence messageToast;
                        if (res.isOperationReussie() == true) {
                            // Element de retour sera de type List<style>
                            Type typeListe = new TypeToken<List<Style>>(){}.getType();

                            // Désérialisation du tableau JSON (JsonArray) en liste d'objets Style

                            //gson.fromJson prend 2 paramètres
                            //Premier paramètre c'est l'élément Json qu'il va falloir récupérer
                            //Deuxième paramètre c'est le type d'élément à récupérer
                            Gson gson = new Gson();
                            List<Style> listeStyles = gson.fromJson(res.get("styles").getAsJsonArray(), typeListe);

                            // Initialisation de l'adapter
                            adapterStyle = new ArrayAdapter<Style>(getActivity().getApplicationContext(), R.layout.support_simple_spinner_dropdown_item);

                            // Ajout de la liste des styles à l'adapter
                            adapterStyle.addAll(listeStyles);

                            // Application de l'adapter au spinner
                            spinStyle.setAdapter(adapterStyle);
                        } else {
                            messageToast = "Échec de la connexion\r\n" + res.getMessage();

                            // Affichage d'un toast pour indiquer le résultat
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), messageToast, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {
                        int statusCode = response.code();

                        String res = response.toString();
                        CharSequence message ="Erreur lors de l'appel à l'API (" + statusCode +")";
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<HomebandApiReponse> call, Throwable t) {
                    Log.d("LoginActivity", t.getMessage());
                }
            });
        } catch (Exception e){
            Toast.makeText(getActivity(),(CharSequence)"Exception",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void getEvents(){
        int var_style = ((Style)(spinStyle.getSelectedItem())).getId_styles();
        String adresse = etAdresse.getText().toString();
        int var_kilometre = Integer.parseInt(etKilometre.getText().toString());
        int var_du = Integer.parseInt(etDu.getText().toString());
        int var_au = Integer.parseInt(etAu.getText().toString());
        try {
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(HomebandRetrofit.API_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            // Création d'une instance du service avec Retrofit
            HomebandApiInterface serviceApi = retrofit.create(HomebandApiInterface.class);
            Log.d("style",String.valueOf(var_style));
            Log.d("cp",adresse);
            Log.d("kilometre",String.valueOf(var_kilometre));
            // Requête vers l'API
            serviceApi.getEvenements(var_style,adresse,var_kilometre,0,0,var_du,var_au).enqueue(new Callback<HomebandApiReponse>() {
                @Override
                public void onResponse(Call<HomebandApiReponse> call, Response<HomebandApiReponse> response) {

                    // En fonction du code HTTP de Retour (2** = Successful)
                    if (response.isSuccessful()) {

                        // Récupération de la réponse de l'API
                        HomebandApiReponse res = response.body();
                        res.mapResultat();

                        CharSequence messageToast;
                        if (res.isOperationReussie() == true) {
                            // Element de retour sera de type List<style>
                            Type typeListe = new TypeToken<List<Groupe>>(){}.getType();

                            // Désérialisation du tableau JSON (JsonArray) en liste d'objets Style

                            //gson.fromJson prend 2 paramètres
                            //Premier paramètre c'est l'élément Json qu'il va falloir récupérer
                            //Deuxième paramètre c'est le type d'élément à récupérer
                            Gson gson = new Gson();
                            List<Evenement> listeEvents = gson.fromJson(res.get("events").getAsJsonArray(), typeListe);
                            Log.d("caca",listeEvents.toString());
                            Intent intent = new Intent (getView().getContext(),SearchEventsResultActivity.class);
                            intent.putExtra("events",(ArrayList<Evenement>)listeEvents);
                            startActivity(intent);





                        } else {
                            messageToast = "Échec de la connexion\r\n" + res.getMessage();

                            // Affichage d'un toast pour indiquer le résultat
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), messageToast, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {
                        int statusCode = response.code();

                        String res = response.toString();
                        CharSequence message ="Erreur lors de l'appel à l'API (" + statusCode +")";
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<HomebandApiReponse> call, Throwable t) {
                    Log.d("LoginActivity", t.getMessage());
                }
            });
        } catch (Exception e){
            Toast.makeText(getActivity(),(CharSequence)"Exception",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

   public void initDate (){

        // Initialisation des calendriers
       calendarDateDu = Calendar.getInstance();
       calendarDateAu = Calendar.getInstance();

       // Ajout des listeners sur les EditText
       etDu.setOnClickListener(this);
       etAu.setOnClickListener(this);

       // Initialisation du formateur de date
       dateFormatter = new SimpleDateFormat("dd/MM/YYYY");

       // Initialisation du DatePickerDialog de la première date (et définition du comportement lors de la sélection)
       pickerDateDu = new DatePickerDialog(getContext(), R.style.ThemeDatePicker, new DatePickerDialog.OnDateSetListener() {
           public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
               Calendar newDate = Calendar.getInstance();
               newDate.set(year, monthOfYear, dayOfMonth);
               etDu.setText(dateFormatter.format(newDate.getTime()));
               // Si datedu > dateau ou dateau vide => dateAu = dateDu

           }
       }, calendarDateDu.get(Calendar.YEAR), calendarDateDu.get(Calendar.MONTH), calendarDateDu.get(Calendar.DAY_OF_MONTH));

       // Initialisation du DatePickerDialog de la deuxième date (et définition du comportement lors de la sélection)
       pickerDateAu = new DatePickerDialog(getContext(), R.style.ThemeDatePicker, new DatePickerDialog.OnDateSetListener() {
           public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
               Calendar newDate = Calendar.getInstance();
               newDate.set(year, monthOfYear, dayOfMonth);
               etAu.setText(dateFormatter.format(newDate.getTime()));
               //DateAu < DateDu alors dateAu = DateDu
           }
       }, calendarDateAu.get(Calendar.YEAR), calendarDateAu.get(Calendar.MONTH), calendarDateAu.get(Calendar.DAY_OF_MONTH));
   }

    public void getLocalisations(){
        double lat;
        double lon;

        HomebandGPSTracker gps = new HomebandGPSTracker(getContext(),this);
        if(gps.canGetLocation()){
            lat = gps.getLatitude();
            lon=gps.getLongitude();
        }
        else{
            gps.showSettingsAlert(2);
            return;
        }
        try {
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(HomebandRetrofit.API_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            // Création d'une instance du service avec Retrofit
            HomebandApiInterface serviceApi = retrofit.create(HomebandApiInterface.class);

            // Requête vers l'API
            serviceApi.getLocalisations(1,null,lat,lon).enqueue(new Callback<HomebandApiReponse>() {
                @Override
                public void onResponse(Call<HomebandApiReponse> call, Response<HomebandApiReponse> response) {

                    // En fonction du code HTTP de Retour (2** = Successful)
                    if (response.isSuccessful()) {

                        // Récupération de la réponse de l'API
                        HomebandApiReponse res = response.body();
                        res.mapResultat();

                        CharSequence messageToast;
                        if (res.isOperationReussie() == true) {
                            // Désérialisation du tableau JSON (JsonArray) en liste d'objets Style

                            //gson.fromJson prend 2 paramètres
                            //Premier paramètre c'est l'élément Json qu'il va falloir récupérer
                            //Deuxième paramètre c'est le type d'élément à récupérer
                            Gson gson = new Gson();
                            String address = res.get("address").getAsString();
                            etAdresse.setText(address);

                        } else {
                            messageToast = "Échec de la connexion\r\n" + res.getMessage();

                            // Affichage d'un toast pour indiquer le résultat
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), messageToast, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {
                        int statusCode = response.code();

                        String res = response.toString();
                        CharSequence message ="Erreur lors de l'appel à l'API (" + statusCode +")";
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<HomebandApiReponse> call, Throwable t) {
                    Log.d("LoginActivity", t.getMessage());
                }
            });
        } catch (Exception e){
            Toast.makeText(getActivity(),(CharSequence)"Exception",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("requestcode",String.valueOf(requestCode));
        Log.d("Permission",String.valueOf(permissions.length));
        Log.d("grantResult",String.valueOf(grantResults.length));
        if (grantResults.length>0){
            switch (requestCode){
                case 15 :
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        getLocalisations();
                    }
                    break;
            }


        }
    }
}
