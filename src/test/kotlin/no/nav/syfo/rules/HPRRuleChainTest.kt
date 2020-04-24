package no.nav.syfo.rules

import io.mockk.mockk
import no.nav.syfo.client.Behandler
import no.nav.syfo.client.Godkjenning
import no.nav.syfo.client.Kode
import no.nav.syfo.model.Dialogmelding
import no.nav.syfo.model.HelsepersonellKategori
import org.amshove.kluent.shouldEqual
import org.junit.Test

internal class HPRRuleChainTest {

    val dialogmelding = mockk<Dialogmelding>()

    fun ruleData(dialogmelding: Dialogmelding, behandler: Behandler) =
        RuleData(dialogmelding, behandler)

    @Test
    internal fun `Should check rule BEHANDLER_IKKE_GYLDIG_I_HPR, should trigger rule`() {
        val behandler = Behandler(
            listOf(
                Godkjenning(
                    autorisasjon = Kode(
                        aktiv = false,
                        oid = 7702,
                        verdi = "1"
                    )
                )
            )
        )

        HPRRuleChain.BEHANDLER_IKKE_GYLDIG_I_HPR(ruleData(dialogmelding, behandler)) shouldEqual true
    }

    @Test
    internal fun `Should check rule BEHANDLER_IKKE_GYLDIG_I_HPR, should NOT trigger rule`() {
        val behandler = Behandler(
            listOf(
                Godkjenning(
                    autorisasjon = Kode(
                        aktiv = true,
                        oid = 7702,
                        verdi = "1"
                    )
                )
            )
        )

        HPRRuleChain.BEHANDLER_IKKE_GYLDIG_I_HPR(ruleData(dialogmelding, behandler)) shouldEqual false
    }

    @Test
    internal fun `Should check rule BEHANDLER_NOT_VALID_AUTHORIZATION_IN_HPR, should trigger rule`() {
        val behandler = Behandler(
            listOf(
                Godkjenning(
                    autorisasjon = Kode(
                        aktiv = true,
                        oid = 7702,
                        verdi = "11"
                    )
                )
            )
        )

        HPRRuleChain.BEHANDLER_MANGLER_AUTORISASJON_I_HPR(ruleData(dialogmelding, behandler)) shouldEqual true
    }

    @Test
    internal fun `Should check rule BEHANDLER_MANGLER_AUTORISASJON_I_HPR, should NOT trigger rule`() {
        val behandler = Behandler(
            listOf(
                Godkjenning(
                    autorisasjon = Kode(
                        aktiv = true,
                        oid = 7704,
                        verdi = "1"
                    )
                )
            )
        )

        HPRRuleChain.BEHANDLER_MANGLER_AUTORISASJON_I_HPR(ruleData(dialogmelding, behandler)) shouldEqual false
    }

    @Test
    internal fun `Should check rule BEHANDLER_MANGLER_AUTORISASJON_I_HPR, should trigger rule`() {
        val behandler = Behandler(
            listOf(
                Godkjenning(
                    autorisasjon = Kode(
                        aktiv = true,
                        oid = 0,
                        verdi = ""
                    ),
                    helsepersonellkategori = Kode(
                        aktiv = true,
                        oid = 0,
                        verdi = "PL"
                    )
                )
            )
        )

        HPRRuleChain.BEHANDLER_MANGLER_AUTORISASJON_I_HPR(ruleData(dialogmelding, behandler)) shouldEqual true
    }

    @Test
    internal fun `Should check rule BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR, should NOT trigger rule`() {
        val behandler = Behandler(
            listOf(
                Godkjenning(
                    autorisasjon = Kode(
                        aktiv = true,
                        oid = 0,
                        verdi = ""
                    ),
                    helsepersonellkategori = Kode(
                        aktiv = true,
                        oid = 0,
                        verdi = HelsepersonellKategori.LEGE.verdi
                    )
                )
            )
        )

        HPRRuleChain.BEHANDLER_IKKE_LE_KI_MT_TL_FT_PS_I_HPR(ruleData(dialogmelding, behandler)) shouldEqual false
    }

    @Test
    internal fun `Should check rule BEHANDLER_IKKE_LE_KI_MT_TL_FT_I_HPR, should trigger rule`() {
        val behandler = Behandler(
            listOf(
                Godkjenning(
                    autorisasjon = Kode(
                        aktiv = true,
                        oid = 0,
                        verdi = ""
                    ),
                    helsepersonellkategori = Kode(
                        aktiv = true,
                        oid = 0,
                        verdi = "kvakksalver"
                    )
                )
            )
        )

        HPRRuleChain.BEHANDLER_IKKE_LE_KI_MT_TL_FT_PS_I_HPR(ruleData(dialogmelding, behandler)) shouldEqual true
    }

    @Test
    internal fun `Should check rule BEHANDLER_IKKE_LE_KI_MT_TL_FT_PS_I_HPR, should NOT trigger rule`() {
        val behandler = Behandler(
            listOf(
                Godkjenning(
                    autorisasjon = Kode(
                        aktiv = true,
                        oid = 0,
                        verdi = ""
                    ),
                    helsepersonellkategori = Kode(
                        aktiv = true,
                        oid = 0,
                        verdi = HelsepersonellKategori.PSYKOLOG.verdi
                    )
                )
            )
        )

        HPRRuleChain.BEHANDLER_IKKE_LE_KI_MT_TL_FT_PS_I_HPR(ruleData(dialogmelding, behandler)) shouldEqual false
    }
}
