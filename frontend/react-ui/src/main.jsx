import React from 'react'
import ReactDOM from 'react-dom/client'
import Home from './Home.jsx'
import { ChakraProvider, createStandaloneToast } from '@chakra-ui/react'
import {createBrowserRouter, RouterProvider } from "react-router-dom";
import './index.css'
import Login from "./components/login/Login.jsx";
import AuthProvider from "./components/context/AuthContext.jsx";
import ProtectedRoute from "./components/shared/ProtectedRoute.jsx";
import Signup from "./components/signup/Signup";
import Member from "./Member.jsx";

const { ToastContainer } = createStandaloneToast()

const router = createBrowserRouter([
    {
        path: "/",
        element: <Login />
    },
    {
        path: "/signup",
        element: <Signup />
    },
    {
        path: "dashboard",
        element: <ProtectedRoute> <Home /> </ProtectedRoute>
    },
    {
        path: "dashboard/members",
        element: <ProtectedRoute> <Member /> </ProtectedRoute>
    }
])

ReactDOM
    .createRoot(document.getElementById('root'))
    .render(
  <React.StrictMode>
      <ChakraProvider>
          <AuthProvider>
              <RouterProvider router={router} />
          </AuthProvider>
          <ToastContainer/>
      </ChakraProvider>
  </React.StrictMode>,
)
