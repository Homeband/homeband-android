package be.heh.homeband.Dao;

import be.heh.homeband.entities.Album;

public interface AlbumDao extends Dao<Integer,Album> {
    void deleteByGroup (int id_groupes);
}
