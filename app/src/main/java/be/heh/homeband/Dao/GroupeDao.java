package be.heh.homeband.Dao;

import java.util.List;

import be.heh.homeband.entities.Groupe;

public interface GroupeDao  extends Dao<Integer,Groupe> {

    List<Groupe> listByUser(int id_utilisateurs);
    boolean isUsed(int id_groupes);
}
