package be.heh.homeband.activities.BandOnClick;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import be.heh.homeband.Dao.AlbumDao;
import be.heh.homeband.Dao.EvenementDao;
import be.heh.homeband.Dao.GroupeDao;
import be.heh.homeband.Dao.MembreDao;
import be.heh.homeband.Dao.StyleDao;
import be.heh.homeband.Dao.TitreDao;
import be.heh.homeband.Dao.UtilisateurDao;
import be.heh.homeband.Dao.UtilisateursGroupesDao;
import be.heh.homeband.Dao.VersionDao;
import be.heh.homeband.Dao.VilleDao;
import be.heh.homeband.DaoImpl.AlbumDaoImpl;
import be.heh.homeband.DaoImpl.EvenementDaoImpl;
import be.heh.homeband.DaoImpl.GroupeDaoImpl;
import be.heh.homeband.DaoImpl.MembreDaoImpl;
import be.heh.homeband.DaoImpl.StyleDaoImpl;
import be.heh.homeband.DaoImpl.TitreDaoImpl;
import be.heh.homeband.DaoImpl.UtilisateurDaoImpl;
import be.heh.homeband.DaoImpl.UtilisateursGroupesDaoImpl;
import be.heh.homeband.DaoImpl.VersionDaoImpl;
import be.heh.homeband.DaoImpl.VilleDaoImpl;
import be.heh.homeband.R;
import be.heh.homeband.activities.ListAlbum.ListAlbumResultActivity;
import be.heh.homeband.activities.LoadingDialog;
import be.heh.homeband.activities.searchevents.SearchEventsResultActivity;
import be.heh.homeband.app.HomebandApiInterface;
import be.heh.homeband.app.HomebandApiReponse;
import be.heh.homeband.app.HomebandRetrofit;
import be.heh.homeband.entities.Album;
import be.heh.homeband.entities.Evenement;
import be.heh.homeband.entities.Groupe;
import be.heh.homeband.entities.Membre;
import be.heh.homeband.entities.Style;
import be.heh.homeband.entities.Titre;
import be.heh.homeband.entities.Utilisateur;
import be.heh.homeband.entities.UtilisateursGroupes;
import be.heh.homeband.entities.Version;
import be.heh.homeband.entities.Ville;
import io.realm.Realm;
import io.realm.RealmQuery;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GroupeDetailsActivity extends AppCompatActivity implements FragmentBio.OnFragmentInteractionListener,FragmentContacts.OnFragmentInteractionListener, FragmentMembres.OnFragmentInteractionListener {

    Button btnMusiques;
    Button btnEvents;

    Boolean isFavorite;

    TextView tvBandName;
    TextView tvBandCity;
    TextView tvBandStyle;

    ImageButton ibFavourite;

    Groupe groupe;
    List<Membre> membres;

    ViewPager viewPager;

    VilleDao villeDao;
    StyleDao styleDao;
    UtilisateurDao utilisateurDao;
    UtilisateursGroupesDao utilisateursGroupesDao;
    GroupeDao groupeDao;
    MembreDao membreDao;
    AlbumDao albumDao;
    TitreDao titreDao;
    EvenementDao evenementDao;

    HashMap<String, Integer> idLiaison;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_onclick);


        //C'est l'objet groupe reçu depuis l'API
        groupe = (Groupe) getIntent().getSerializableExtra("groupe");
        membres = (ArrayList<Membre>) getIntent().getSerializableExtra("members");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        init();
        bindData(groupe);
        setTitle(groupe.getNom());
        // Locate the viewpager in activity_main.xml
        viewPager = (ViewPager) findViewById(R.id.pager);

        // Set the ViewPagerAdapter into ViewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FragmentBio(), "Biographie");
        adapter.addFrag(new FragmentMembres(), "Membres");
        adapter.addFrag(new FragmentContacts(), "Contacts");

        viewPager.setAdapter(adapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.pager_header);
        mTabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onSupportNavigateUp(){

        this.finish();
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void init(){


        tvBandName = (TextView) findViewById(R.id.tvBandName);
        tvBandCity = (TextView) findViewById(R.id.tvBandCity);
        tvBandStyle = (TextView) findViewById(R.id.tvBandStyle);

        villeDao = new VilleDaoImpl();
        styleDao = new StyleDaoImpl();
        utilisateurDao = new UtilisateurDaoImpl();
        utilisateursGroupesDao = new UtilisateursGroupesDaoImpl();
        groupeDao = new GroupeDaoImpl();
        membreDao = new MembreDaoImpl();
        albumDao = new AlbumDaoImpl();
        titreDao = new TitreDaoImpl();
        evenementDao = new EvenementDaoImpl();

        btnMusiques = (Button) findViewById(R.id.btnMusiques);
        btnMusiques.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getAlbums(groupe.getId_groupes());
            }
        });

        btnEvents = (Button) findViewById(R.id.btnEvents);
        btnEvents.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getGroupeEvents(groupe.getId_groupes());
            }
        });

        final Utilisateur user = utilisateurDao.getConnectedUser();
        ibFavourite = (ImageButton)  findViewById(R.id.ibFavourite);
        ibFavourite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isFavorite) {
                    add_liaison(user.getId_utilisateurs(), groupe.getId_groupes());
                    ibFavourite.setBackgroundResource(R.drawable.round_favourite);

                } else {
                    remove_liaison(user.getId_utilisateurs(), groupe.getId_groupes());

                }
            }
        });




        idLiaison = new HashMap<String, Integer>();
        idLiaison.put(UtilisateursGroupesDaoImpl.KEY_UTILISATEUR, user.getId_utilisateurs());
        idLiaison.put(UtilisateursGroupesDaoImpl.KEY_GROUPE, groupe.getId_groupes());
        if(utilisateursGroupesDao.get(idLiaison) == null){
            isFavorite = false;
        } else {
            isFavorite = true;
            ibFavourite.setBackgroundResource(R.drawable.round_favourite);
        }


    }

    public void bindData(Groupe groupe){

       Ville ville = villeDao.get(groupe.getId_villes());
       Style style = styleDao.get(groupe.getId_styles());

        tvBandName.setText(groupe.getNom());
        tvBandCity.setText(ville.getNom());
        tvBandStyle.setText(style.getNom());
    }



    public void remove_liaison(final int id_utilisateur, final int id_groupe){
        final DialogFragment loading = new LoadingDialog();
        android.app.FragmentManager frag = ((AppCompatActivity) this).getFragmentManager();
        loading.show(frag,"LoadingDialog");
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(HomebandRetrofit.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Création d'une instance du service avec Retrofit
            HomebandApiInterface serviceApi = retrofit.create(HomebandApiInterface.class);

            // Requête vers l'API
            serviceApi.removeUtilisateurGroupe(id_utilisateur,id_groupe).enqueue(new Callback<HomebandApiReponse>() {
                @Override
                public void onResponse(Call<HomebandApiReponse> call, Response<HomebandApiReponse> response) {


                    // En fonction du code HTTP de Retour (2** = Successful)
                    if (response.isSuccessful()) {

                        // Récupération de la réponse de l'API
                        HomebandApiReponse res = response.body();
                        res.mapResultat();

                        CharSequence messageToast;
                        if (res.isOperationReussie() == true) {

                            //1. Supression liaison locale
                            utilisateursGroupesDao.delete(idLiaison);

                            //2. Supression Membres local
                            membreDao.deleteByGroup(id_groupe);

                            //3. Supression Album
                            albumDao.deleteByGroup(id_groupe);

                            //4. Supression Titres
                            titreDao.deleteByGroup(id_groupe);

                            //5. Modofication bouton favoris
                            ibFavourite.setBackgroundResource(R.drawable.round_disabled);

                            //6. Supression groupe local
                            if(evenementDao.listByGroup(id_groupe).isEmpty()){
                                groupeDao.delete(id_groupe);
                            }

                            isFavorite = false;
                            loading.dismiss();
                        } else {
                            loading.dismiss();
                            messageToast = "Échec de la connexion\r\n" + res.getMessage();

                            // Affichage d'un toast pour indiquer le résultat
                            Toast toast = Toast.makeText(getApplicationContext(), messageToast, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {
                        loading.dismiss();
                        int statusCode = response.code();

                        String res = response.toString();
                        CharSequence message ="Erreur lors de l'appel à l'API (" + statusCode +")";
                        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<HomebandApiReponse> call, Throwable t) {
                    loading.dismiss();
                    Log.d("LoginActivity", t.getMessage());
                }
            });
        } catch (Exception e){
            loading.dismiss();
            Toast.makeText(this,(CharSequence)"Exception",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    public void add_liaison(final int id_utilisateur, final int id_groupe){
        final DialogFragment loading = new LoadingDialog();
        android.app.FragmentManager frag = ((AppCompatActivity) this).getFragmentManager();
        loading.show(frag,"LoadingDialog");
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(HomebandRetrofit.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Création d'une instance du service avec Retrofit
            HomebandApiInterface serviceApi = retrofit.create(HomebandApiInterface.class);

            // Requête vers l'API
            serviceApi.addUtilisateurGroupe(id_utilisateur,id_groupe,0,0,1,1).enqueue(new Callback<HomebandApiReponse>() {
                @Override
                public void onResponse(Call<HomebandApiReponse> call, Response<HomebandApiReponse> response) {


                    // En fonction du code HTTP de Retour (2** = Successful)
                    if (response.isSuccessful()) {

                        // Récupération de la réponse de l'API
                        HomebandApiReponse res = response.body();
                        res.mapResultat();

                        CharSequence messageToast;
                        if (res.isOperationReussie() == true) {

                            //1. Récupération info API
                            Type typeListe = new TypeToken<List<Album>>(){}.getType();
                            Type typeListeTitre = new TypeToken<List<Titre>>(){}.getType();
                            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                            List<Album> listeAlbums = gson.fromJson(res.get("albums").getAsJsonArray(), typeListe);
                            List<Titre> listeTitres = gson.fromJson(res.get("titles").getAsJsonArray(), typeListeTitre);

                            //2. Création liaison locale
                            UtilisateursGroupes liaison = new UtilisateursGroupes();
                            liaison.setId_groupes(id_groupe);
                            liaison.setId_utilisateurs(id_utilisateur);
                            utilisateursGroupesDao.write(liaison);

                            //3. Ajout groupe local
                            groupeDao.write(groupe);

                            //4. Ajout Membres
                            for(int i=0;i<membres.size();i++){
                                membreDao.write(membres.get(i)); }

                            //5. Ajout Album
                            for(int i=0;i<listeAlbums.size();i++){
                                albumDao.write(listeAlbums.get(i)); }

                            //6. Ajout Titres
                            for(int i=0;i<listeTitres.size();i++){
                                titreDao.write(listeTitres.get(i)); }

                            //7. Modofication bouton favoris
                            ibFavourite.setBackgroundResource(R.drawable.round_favourite);


                            isFavorite = true;
                            loading.dismiss();

                        } else {
                            loading.dismiss();
                            messageToast = "Échec de la connexion\r\n" + res.getMessage();

                            // Affichage d'un toast pour indiquer le résultat
                            Toast toast = Toast.makeText(getApplicationContext(), messageToast, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {
                        loading.dismiss();
                        int statusCode = response.code();

                        String res = response.toString();
                        CharSequence message ="Erreur lors de l'appel à l'API (" + statusCode +")";
                        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<HomebandApiReponse> call, Throwable t) {
                    loading.dismiss();

                    Log.d("LoginActivity", t.getMessage());
                }
            });
        } catch (Exception e){
            loading.dismiss();
            Toast.makeText(this,(CharSequence)"Exception",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void getAlbums(int id){


        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(HomebandRetrofit.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Création d'une instance du service avec Retrofit
            HomebandApiInterface serviceApi = retrofit.create(HomebandApiInterface.class);

            // Requête vers l'API
            serviceApi.getAlbums(id).enqueue(new Callback<HomebandApiReponse>() {
                @Override
                public void onResponse(Call<HomebandApiReponse> call, Response<HomebandApiReponse> response) {
                    boolean toUpdate=false;

                    // En fonction du code HTTP de Retour (2** = Successful)
                    if (response.isSuccessful()) {

                        // Récupération de la réponse de l'API
                        HomebandApiReponse res = response.body();
                        res.mapResultat();

                        CharSequence messageToast;
                        if (res.isOperationReussie() == true) {
                            Type typeListe = new TypeToken<List<Album>>(){}.getType();
                            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                            List<Album> listeAlbums = gson.fromJson(res.get("albums").getAsJsonArray(), typeListe);
                            Intent intent = new Intent (getApplicationContext(),ListAlbumResultActivity.class);
                            intent.putExtra("albums",(ArrayList<Album>)listeAlbums);
                            startActivity(intent);


                        } else {

                            messageToast = "Échec de la connexion\r\n" + res.getMessage();

                            // Affichage d'un toast pour indiquer le résultat
                            Toast toast = Toast.makeText(getApplicationContext(), messageToast, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {

                        int statusCode = response.code();

                        String res = response.toString();
                        CharSequence message ="Erreur lors de l'appel à l'API (" + statusCode +")";
                        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
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

            Toast.makeText(this,(CharSequence)"Exception",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void getGroupeEvents(int id){


        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(HomebandRetrofit.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Création d'une instance du service avec Retrofit
            HomebandApiInterface serviceApi = retrofit.create(HomebandApiInterface.class);

            // Requête vers l'API
            serviceApi.getGroupEvents(id).enqueue(new Callback<HomebandApiReponse>() {
                @Override
                public void onResponse(Call<HomebandApiReponse> call, Response<HomebandApiReponse> response) {
                    boolean toUpdate=false;

                    // En fonction du code HTTP de Retour (2** = Successful)
                    if (response.isSuccessful()) {

                        // Récupération de la réponse de l'API
                        HomebandApiReponse res = response.body();
                        res.mapResultat();

                        CharSequence messageToast;
                        if (res.isOperationReussie() == true) {
                            Type typeListe = new TypeToken<List<Evenement>>(){}.getType();
                            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                            List<Evenement> listeEvents = gson.fromJson(res.get("events").getAsJsonArray(), typeListe);
                            Intent intent = new Intent (getApplicationContext(),SearchEventsResultActivity.class);
                            intent.putExtra("events",(ArrayList<Evenement>)listeEvents);
                            startActivity(intent);

                        } else {

                            messageToast = "Échec de la connexion\r\n" + res.getMessage();

                            // Affichage d'un toast pour indiquer le résultat
                            Toast toast = Toast.makeText(getApplicationContext(), messageToast, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {

                        int statusCode = response.code();

                        String res = response.toString();
                        CharSequence message ="Erreur lors de l'appel à l'API (" + statusCode +")";
                        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
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

            Toast.makeText(this,(CharSequence)"Exception",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
