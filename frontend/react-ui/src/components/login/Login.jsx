import {
    Alert,
    AlertIcon,
    Box,
    Button,
    Flex,
    FormLabel,
    Heading,
    Image,
    Input,
    Link,
    Stack
} from '@chakra-ui/react';
import {Formik, Form, useField} from "formik";
import * as Yup from 'yup';
import {useAuth} from "../context/AuthContext.jsx";
import {errorNotification} from "../../services/notification.js";
import {useNavigate} from "react-router-dom";
import {useEffect} from "react";
import appLogo from "../../assets/app_logo.jpg"

const MyTextInput = ({label, ...props}) => {
    const [field, meta] = useField(props);
    return (
        <Box>
            <FormLabel htmlFor={props.id || props.name}>{label}</FormLabel>
            <Input className="text-input" {...field} {...props} />
            {meta.touched && meta.error ? (
                <Alert className="error" status={"error"} mt={2}>
                    <AlertIcon/>
                    {meta.error}
                </Alert>
            ) : null}
        </Box>
    );
};

const LoginForm = () => {
    const { login } = useAuth();
    const navigate = useNavigate();

    return (
        <Formik
            validateOnMount={true}
            validationSchema={
                Yup.object({
                    username: Yup.string()
                        .email("Must be valid email")
                        .required("Email is required"),
                    password: Yup.string()
                        .max(20, "Password cannot be more than 20 characters")
                        .required("Password is required")
                })
            }
            initialValues={{username: '', password: ''}}
            onSubmit={async (values, {setSubmitting}) => {
                setSubmitting(true);
                try {
                    const res = await login(values);
                    navigate("/dashboard");
                    console.log("Successfully logged in");
                } catch (err) {
                    if (err.response && err.response.data) {
                        errorNotification(err.code, err.response.data.message || "An error occurred");
                    } else if (err.response) {
                        errorNotification(err.code, "Response data is undefined. An error occurred.");
                    } else {
                        console.error("Unexpected error:", err);
                        errorNotification(err.code, "An error occurred");
                    }
                } finally {
                    setSubmitting(false);
                }
            }}>

            {({isValid, isSubmitting}) => (
                <Form>
                    <Stack mt={15} spacing={15}>
                        <MyTextInput
                            label={"Email"}
                            name={"username"}
                            type={"email"}
                            placeholder={"jane@formik.com"}
                        />
                        <MyTextInput
                            label={"Password"}
                            name={"password"}
                            type={"password"}
                            placeholder={"Enter your password"}
                        />

                        <Button
                            type={"submit"}
                            disabled={!isValid || isSubmitting}>
                            Login
                        </Button>
                    </Stack>
                </Form>
            )}
        </Formik>
    )
}

const Login = () => {

    const { member } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (member) {
            navigate("/dashboard/members");
        }
    })

    return (
        <Stack minH={'100vh'} direction={{ base: 'column', md: 'row' }}>
            <Flex p={8} flex={1} align={'center'} justify={'center'}>
                <Stack spacing={4} w={'full'} maxW={'md'}>
                    <Image
                    src={appLogo}
                    boxSize={"200px"}
                    alt={"Logo"}
                    alignSelf={"center"}
                    />
                    <Heading fontSize={'2xl'}>Sign in to your account</Heading>
                    <LoginForm/>
                    Don't have an account?
                    <Link color={"blue.500"} href={"/signup"}> Signup now. </Link>
                </Stack>
            </Flex>
        </Stack>
    );
}

export default Login;