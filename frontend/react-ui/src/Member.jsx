import {useEffect, useState} from "react";
import {getMembers} from "./services/client.js";
import {errorNotification} from "./services/notification.js";
import SidebarWithHeader from "./components/shared/SideBar.jsx";
import {Spinner, Text, Wrap, WrapItem} from "@chakra-ui/react";
import CreateMemberDrawer from "./components/member/CreateMemberDrawer.jsx";
import CardWithImage from "./components/member/MemberCard.jsx";

const Member = () => {

    const[members, setMembers] = useState([]);
    const[loading, setLoading] = useState(false);
    const[err, setError] = useState("");

    const fetchMembers = () => {
        setLoading(true);
        getMembers().then(res => {
            setMembers(res.data)
        }).catch(err => {
            setError(err.response.data.message)
            errorNotification(
                err.code,
                err.response.data.message
            )
        }).finally(() => {
            setLoading(false)
        })
    }

    useEffect(() => {
        fetchMembers();
    }, []);

    // loading spinner
    if(loading){
        return (
            <SidebarWithHeader>
                <Spinner thickness='4px'
                         speed='0.65s'
                         emptyColor='gray.200'
                         color='blue.500'
                         size='xl'
                />
            </SidebarWithHeader>
        )
    }

    if(err){
        return (
            <SidebarWithHeader>
                <CreateMemberDrawer
                    fetchMembers = {fetchMembers}
                />
                <Text mt={5}> There was an error!</Text>
            </SidebarWithHeader>
        )
    }

    if(members.length <= 0){
        return (
            <SidebarWithHeader>
                <CreateMemberDrawer
                    fetchMembers = {fetchMembers}
                />
                <Text> No members exist</Text>
            </SidebarWithHeader>
        )
    }

    return (
        <SidebarWithHeader>
            <CreateMemberDrawer
                fetchMember = {fetchMembers}
            />
            <Wrap justify={"center"} spacing={"30px"}>
                {members.map((member, index) => (
                    <WrapItem key={index}>
                        <CardWithImage
                            {...member}
                            imageNumber = {index}
                            fetchMembers = {fetchMembers}
                        />
                    </WrapItem>
                ))}
            </Wrap>
        </SidebarWithHeader>
    )
}

export default Member;