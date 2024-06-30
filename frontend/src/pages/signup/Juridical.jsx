import { useState } from 'react';
import { Container, Row, Col, Alert, Button, Form } from 'react-bootstrap';
import { Formik } from 'formik';
import Api from '../../utils/api';
import { Link } from 'react-router-dom';

const container = {
    backgroundColor: '#FFFFFF', padding: '2rem', marginTop: '4rem',
    boxShadow: '4px 4px 4px lightgray', borderRadius: '6px', width: '40%'
}

const coluna = {
    margin: '0.5rem'
}

const botao = {
    backgroundColor: '#5E3797', border: '0', width: '100%'
}


export default function Juridical() {

    const [accountType] = useState("JURIDICA");

    const [cnpj, setCNPJ] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState("");
    const [message, setMessage] = useState("");


    function handleSubmit() {

        Api.post("/user/createAccount", { cnpj, user: cnpj, password, accountType })

            .then((body) => {
                console.log(body.data)
                setError("");
                setMessage(body.data.message);
                setTimeout(() => {
                    setMessage("");
                }, 15000)

            }).catch((err) => {
                setMessage("");
                setError("Erro: " + err);
                console.log("Erro: " + err);
            });

    }


    return (
        <Formik
            initialValues={{ cnpj: '', password: '' }}
            onSubmit={(values) => {
                handleSubmit();
            }}
        >
            {({
                handleSubmit,
                handleChange,
                values,
                touched,
                errors,
            }) => (

                <Form onSubmit={handleSubmit} onChange={handleChange}>
                    <Container style={container}>
                        <Row style={{ padding: '1rem' }}>
                            <h1 className="display-6" style={{ textAlign: 'center' }}> Conta Jurídica </h1>
                            {error ? (
                                <Alert size="sm" variant="danger">{error}</Alert>
                            ) : null}

                            {message ? (
                                <Alert size="sm" variant="success"> {message} </Alert>
                            ) : null}
                        </Row>
                        <Row>
                            <Col md style={coluna}>
                                <Form.Label>CNPJ</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="CNPJ"
                                    name="cnpj"
                                    value={values.cnpj}
                                    onChange={(e) => setCNPJ(e.target.value)}
                                />
                            </Col>
                        </Row>
                        <Row>
                            <Col md style={coluna}>
                                <Form.Label>Senha</Form.Label>
                                <Form.Control
                                    type="password"
                                    placeholder="Senha"
                                    name="password"
                                    value={values.password}
                                    onChange={(e) => setPassword(e.target.value)}
                                />

                            </Col>
                        </Row>
                        <Col style={{ textAlign: 'center', marginTop: '2rem' }}>
                            <Button size="lg" style={botao} type="submit"> Cadastrar </Button>
                        </Col>
                        <Col style={{ textAlign: 'center', marginTop: '1rem' }}>
                            <p> Você já tem uma conta? <Link to="/login"> Login </Link></p>
                        </Col>
                    </Container>
                </Form>
            )}
        </Formik>
    );
}