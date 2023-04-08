import {Container, Nav, Navbar} from "react-bootstrap";

export const NavBar = () => {

    return(
        <Navbar collapseOnSelect bg="light" expand="md" sticky="top" style={{height: "50px"}} className={"mynavbar"}>
            <Container>
                <Navbar.Brand href="#home">Control Application</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav" >
                    <Nav className="ms-auto bg-light">
                        <Nav.Link href="/ui/taglist">Data Points</Nav.Link>
                        <Nav.Link href="/ui/intlist">Interfaces</Nav.Link>
                        <Nav.Link href="/ui/models">Models</Nav.Link>
                        <Nav.Link href="/ui/controllers">Controllers</Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );

}