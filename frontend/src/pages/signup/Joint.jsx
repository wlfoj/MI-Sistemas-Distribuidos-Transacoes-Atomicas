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


export default function Joint() {

    const [accountType] = useState("CONJUNTA");

    const [cpf1, setCPF1] = useState("");
    const [cpf2, setCPF2] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState("");
    const [message, setMessage] = useState("");


    function handleSubmit() {

        Api.post("/user/createAccount", { cpf1, cpf2, user: cpf1 + cpf2, password, accountType })

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
            initialValues={{ cpf1: '', cpf2: '', password: '' }}
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
                            <h1 className="display-6" style={{ textAlign: 'center' }}> Conta Conjunta </h1>
                            {error ? (
                                <Alert size="sm" variant="danger">{error}</Alert>
                            ) : null}

                            {message ? (
                                <Alert size="sm" variant="success"> {message} </Alert>
                            ) : null}
                        </Row>
                        <Row>
                            <Col md style={coluna}>
                                <Form.Label>CPF 1</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="CPF 1"
                                    name="cpf1"
                                    value={values.cpf1}
                                    onChange={(e) => setCPF1(e.target.value)}
                                />
                            </Col>
                            <Col md style={coluna}>
                                <Form.Label>CPF 2</Form.Label>
                                <Form.Control
                                    type="text"
                                    placeholder="CPF 2"
                                    name="cpf2"
                                    value={values.cpf2}
                                    onChange={(e) => setCPF2(e.target.value)}
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