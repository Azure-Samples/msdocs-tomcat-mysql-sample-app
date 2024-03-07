package com.microsoft.azure.appservice.examples.tomcatmysql;

import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.appservice.examples.tomcatmysql.models.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns = "/")
public class ViewServlet extends HttpServlet {
    private static Logger logger = LogManager.getLogger(ViewServlet.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        logger.info("GET /");

        EntityManagerFactory emf = (EntityManagerFactory) req.getServletContext().getAttribute("EMFactory");
        // logger.info("req.getServletContext().getAttribute done");
        EntityManager em = emf.createEntityManager();
        // logger.info("emf.createEntityManager done");
        EntityTransaction transaction = em.getTransaction();
        // logger.info("em.getTransaction done");
        try {
            transaction.begin();
            // logger.info("transaction.begin done");
            List<Student> students = em.createQuery("SELECT a FROM Student a", Student.class).getResultList();
            // logger.info("em.createQuery done");
            transaction.commit();
            // logger.info("transaction.commit done");
            req.setAttribute("studentRecords", students);
            // logger.info("req.setAttribute done");
        } catch (Exception e) {
            logger.info("exception thrown: " + e.getMessage());
            if(transaction != null && transaction.isActive()) {
                transaction.rollback();
                // logger.info("transaction.rollback done");
            }
            throw e;
        } finally {
            em.close();
            // logger.info("em.close done");
        }

        req.getRequestDispatcher("/WEB-INF/views/viewStudents.jsp").forward(req, resp);
        // logger.info("req.getRequestDispatcher done");
    }
}
