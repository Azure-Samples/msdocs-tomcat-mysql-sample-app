package com.microsoft.azure.appservice.examples.tomcatmysql;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.appservice.examples.tomcatmysql.models.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet(urlPatterns = "/edit")
public class EditServlet extends HttpServlet {

    private static Logger logger = LogManager.getLogger(EditServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("GET /edit");

        String idStr = req.getParameter("id");
        if (idStr == null)
            throw new ServletException("Error: parameters missing.");
        Long id = Long.parseLong(idStr);

        EntityManagerFactory emf = (EntityManagerFactory) req.getServletContext().getAttribute("EMFactory");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            req.setAttribute("studentRecord", em.find(Student.class, id));
        } catch (Exception e) {
            if(transaction != null && transaction.isActive())
                transaction.rollback();
            throw e;
        } finally {
            em.close();
        }

        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/views/editStudent.jsp");
        dispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("POST /edit");

        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String stdStr = req.getParameter("std");

        if (idStr == null || name == null || stdStr == null)
            throw new ServletException("Error: parameters missing.");
        Long id = Long.parseLong(idStr);
        int std = Integer.parseInt(stdStr);

        EntityManagerFactory emf = (EntityManagerFactory) req.getServletContext().getAttribute("EMFactory");
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            Student student = em.find(Student.class, id);
            if(student != null) {
                student.setName(name);
                student.setStd(std);
                transaction.commit();
            }
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
