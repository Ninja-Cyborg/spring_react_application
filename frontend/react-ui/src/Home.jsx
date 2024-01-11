import { Text} from '@chakra-ui/react'
import SidebarWithHeader from "./components/shared/SideBar.jsx";

const Home = () => {
    return (
        <SidebarWithHeader>
            <Text fontSize={"5xl"}>Dashboard</Text>
        </SidebarWithHeader>
    )
}

export default Home;