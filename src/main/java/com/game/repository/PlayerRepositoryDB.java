package com.game.repository;

import com.game.entity.Player;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        properties.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        properties.put(Environment.DRIVER, "org.postgresql.Driver");
        properties.put(Environment.URL, "jdbc:postgresql://localhost:5432/hibernate");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        properties.put(Environment.DEFAULT_SCHEMA, "rpg");
        properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.USER, "goldin");
        properties.put(Environment.PASS, "goldin");
//        Properties properties = new Properties();
//        try (InputStream input = getClass().getClassLoader().getResourceAsStream("hibernate.properties")) {
//            if (input == null) {
//                throw new RuntimeException("Файл hibernate.properties не найден");
//            }
//            properties.load(input);
//        } catch (IOException e) {
//            throw new RuntimeException("Ошибка загрузки hibernate.properties", e);
//        }
        sessionFactory = new Configuration()
//                .setProperties(properties)
                .addProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try (Session session = sessionFactory.openSession()) {
            NativeQuery<Player> query = session.createNativeQuery("SELECT * FROM rpg.player", Player.class);
            query.setFirstResult(pageNumber * pageSize);
            query.setMaxResults(pageSize);
            return query.list();
        }
    }

    @Override
    public int getAllCount() {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createNamedQuery("countPlayer", Long.class);
            return query != null ? query.uniqueResult().intValue() : 0;
        }
    }

    @Override
    public Player save(Player player) {
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.persist(player);
            session.getTransaction().commit();
            session.close();
            return player;
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player update(Player player) {
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.merge(player);
            session.getTransaction().commit();
            session.close();
            return player;
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public Optional<Player> findById(long id) {
       Session session = sessionFactory.openSession();
       session.beginTransaction();
        Player player = session.get(Player.class, id);
        session.getTransaction().commit();
        session.close();
        return Optional.ofNullable(player);
    }

    @Override
    public void delete(Player player) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.delete(player);
        session.getTransaction().commit();
        session.close();
    }

    @PreDestroy
    public void beforeStop() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}