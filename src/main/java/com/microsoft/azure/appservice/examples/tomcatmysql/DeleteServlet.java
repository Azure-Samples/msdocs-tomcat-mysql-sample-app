package com.microsoft.azure.appservice.examples.tomcatmysql;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.appservice.examples.tomcatmysql.models.Student;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns = "/delete")
public class DeleteServlet extends HttpServlet {

    private static Logger logger = LogManager.getLogger(DeleteServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /delete");

        String idStr = req.getParameter("id");
        if (idStr == null)
            throw new ServletException("Error: id parameter missing.");
        Long id = Long.parseLong(idStr);

        EntityManagerFactory emf = (EntityManagerFactory) req.getServletContext().getAttribute("EMFactory");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Student student = em.find(Student.class, id);
            if(student != null) {
                em.remove(student);
            }
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
