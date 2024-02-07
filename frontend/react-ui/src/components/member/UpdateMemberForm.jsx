import {Form, Formik, useField} from 'formik';
import * as Yup from 'yup';
import {Alert, AlertIcon, Box, Button, FormLabel, Image, Input, Stack, VStack} from "@chakra-ui/react";
import {memberProfileImageUrl, updateMember, uploadProfileImage} from "../../services/client.js";
import {successNotification, errorNotification} from "../../services/notification.js";
import {useDropzone} from "react-dropzone";
import {useCallback} from "react";

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

const FileDropzone = ({id, fetchMembers}) => {
    const onDrop = useCallback(acceptedFiles => {
        const formData = new FormData();
        formData.append("file", acceptedFiles[0])

        uploadProfileImage(
            id,
            formData
        ).then(() => {
            successNotification("Success", "Profile picture uploaded")
            fetchMembers() // updating current image in ui
        }).catch(() => {
            errorNotification("Error", "Failed to upload the image!")
        })
    }, [])
    const {getRootProps, getInputProps, isDragActive} = useDropzone({onDrop})

    return (
        <Box {...getRootProps()}
             w={'100%'}
             textAlign={'center'}
             border={'dashed'}
             borderColor={'blue.200'}
             borderRadius={'3xl'}
             p={6}
             rounded={'md'}
        >
            <input {...getInputProps()} />
            {
                isDragActive ?
                    <p>Drop the files here ...</p> :
                    <p>Drag 'n' drop Profile Image here, or click to select Image</p>
            }
        </Box>
    )
}

const UpdateMemberForm = ({ fetchMembers, initialValues, memberId }) => {
    return (
        <>
            <VStack spacing={'4'} mb={'5'}>
                <Image
                    borderRadius={'full'}
                    boxSize={'150px'}
                    objectFit={'cover'}
                    src={memberProfileImageUrl(memberId)}
                />
                <FileDropzone
                    id = {memberId}
                    fetchMembers={fetchMembers}
                />
            </VStack>
            <Formik
                initialValues={initialValues}
                validationSchema={Yup.object({
                    name: Yup.string()
                        .max(15, 'Must be 15 characters or less')
                        .required('Required'),
                    email: Yup.string()
                        .email('Must be 20 characters or less')
                        .required('Required'),
                    age: Yup.number()
                        .min(16, 'Must be at least 16 years of age')
                        .max(100, 'Must be less than 100 years of age')
                        .required('Required'),
                })}
                onSubmit={(updatedMember, {setSubmitting}) => {
                    setSubmitting(true);
                    updateMember(memberId, updatedMember)
                        .then(res => {
                            console.log(res);
                            successNotification(
                                "Member updated",
                                `${updatedMember.name} was successfully updated`
                            )
                            fetchMembers();
                        }).catch(err => {
                        console.log(err);
                        errorNotification(
                            err.code,
                            err.response.data.message
                        )
                    }).finally(() => {
                        setSubmitting(false);
                    })
                }}
            >
                {({isValid, isSubmitting, dirty}) => (
                    <Form>
                        <Stack spacing={"24px"}>
                            <MyTextInput
                                label="Name"
                                name="name"
                                type="text"
                                placeholder="Jane"
                            />

                            <MyTextInput
                                label="Email Address"
                                name="email"
                                type="email"
                                placeholder="jane@formik.com"
                            />

                            <MyTextInput
                                label="Age"
                                name="age"
                                type="number"
                                placeholder="17"
                            />

                            <Button disabled={!(isValid && dirty) || isSubmitting} type="submit">Submit</Button>
                        </Stack>
                    </Form>
                )}
            </Formik>
        </>
    );
};

export default UpdateMemberForm;