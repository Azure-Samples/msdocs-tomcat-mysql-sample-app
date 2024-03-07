package com.microsoft.azure.appservice.examples.tomcatmysql;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.appservice.examples.tomcatmysql.models.Task;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns = "/create")
public class CreateServlet extends HttpServlet  {

    private static Logger logger = LogManager.getLogger(CreateServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /create");

        String name = req.getParameter("name");

        if (name == null)
            throw new ServletException("Error: parameters missing.");

        EntityManagerFactory emf = (EntityManagerFactory) req.getServletContext().getAttribute("EMFactory");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Task task = new Task();
            task.setName(name);
            em.persist(task);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null && transaction.isActive())
                transaction.rollback();
            throw e;
        } finally {
            em.close();
        }

        String path = req.getContextPath();
        if(path != "") {
            resp.sendRedirect(path);
        } else {
            resp.sendRedirect("/");
        }
    }
}
