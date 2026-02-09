package it.contia4zampe.simulator.engine

interface Dado {
    fun lancia(): Int
}


class DadoStandard : Dado {
    override fun lancia(): Int = (1..6).random()
}


class DadoFisso(private val valore: Int) : Dado {
    override fun lancia() = valore
}
