package be.heh.homeband.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.EventLog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import be.heh.homeband.R;
import be.heh.homeband.activities.searchevents.SearchEventsResultActivity;
import be.heh.homeband.activities.searchgroup.SearchGroupResultActivity;
import be.heh.homeband.app.HomebandApiInterface;
import be.heh.homeband.app.HomebandApiReponse;
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
        //if(v == btnRecherche){
        //TODO appel api
        List<Evenement> events = new ArrayList<Evenement>();
        events.add(new Evenement(1, "Groupe 1"));
        events.add(new Evenement(2, "Groupe 2"));
        events.add(new Evenement(3, "Groupe 3"));
        events.add(new Evenement(4, "Groupe 4"));
        Intent intent = new Intent (getView().getContext(),SearchEventsResultActivity.class);
        intent.putExtra("events",(ArrayList<Evenement>)events);
        startActivity(intent);
        //}
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
        btnRecherche = (Button) myview.findViewById(R.id.btnRechercheEvents);

        btnRecherche.setOnClickListener(this);

        spinStyle = (Spinner) myview.findViewById(R.id.spinnerStyle);
        Switch s = (Switch) myview.findViewById(R.id.switch1);

        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    EditText Du = (EditText) getView().findViewById(R.id.etDu);
                    EditText Au = (EditText) getView().findViewById(R.id.etAu);
                    TextView Dut = (TextView) getView().findViewById(R.id.textView);
                    TextView Aut = (TextView) getView().findViewById(R.id.textView5);
                    Du.setVisibility(View.VISIBLE);
                    Au.setVisibility(View.VISIBLE);
                    Dut.setVisibility(View.VISIBLE);
                    Aut.setVisibility(View.VISIBLE);
                }
                else   {
                    EditText Du = (EditText) getView().findViewById(R.id.etDu);
                    EditText Au = (EditText) getView().findViewById(R.id.etAu);
                    TextView Dut = (TextView) getView().findViewById(R.id.textView);
                    TextView Aut = (TextView) getView().findViewById(R.id.textView5);
                    Du.setVisibility(View.INVISIBLE);
                    Au.setVisibility(View.INVISIBLE);
                    Dut.setVisibility(View.INVISIBLE);
                    Aut.setVisibility(View.INVISIBLE);
                }


            }
        });
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
}
