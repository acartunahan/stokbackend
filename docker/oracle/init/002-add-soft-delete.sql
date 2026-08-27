WHENEVER SQLERROR EXIT SQL.SQLCODE

ALTER SESSION SET CONTAINER = FREEPDB1;

DECLARE
    PROCEDURE add_soft_delete_column(p_table_name VARCHAR2) IS
        v_table_count NUMBER;
        v_column_count NUMBER;
    BEGIN
        SELECT COUNT(*)
          INTO v_table_count
          FROM ALL_TABLES
         WHERE OWNER = 'STOKTAKIP'
           AND TABLE_NAME = p_table_name;

        IF v_table_count = 0 THEN
            RETURN;
        END IF;

        SELECT COUNT(*)
          INTO v_column_count
          FROM ALL_TAB_COLUMNS
         WHERE OWNER = 'STOKTAKIP'
           AND TABLE_NAME = p_table_name
           AND COLUMN_NAME = 'SILINDI';

        IF v_column_count = 0 THEN
            EXECUTE IMMEDIATE
                'ALTER TABLE STOKTAKIP.' || DBMS_ASSERT.SIMPLE_SQL_NAME(p_table_name) ||
                ' ADD (SILINDI BOOLEAN DEFAULT FALSE NOT NULL)';
        END IF;
    END;
BEGIN
    add_soft_delete_column('APARAT');
    add_soft_delete_column('DEPO');
    add_soft_delete_column('DEPO_ZIMMET_HAREKET');
    add_soft_delete_column('ENVANTER');
    add_soft_delete_column('ENVANTER_ACIKLAMA');
    add_soft_delete_column('ENVANTER_DETAY');
    add_soft_delete_column('FIRMA');
    add_soft_delete_column('IHALE');
    add_soft_delete_column('KATEGORI');
    add_soft_delete_column('MARKA');
    add_soft_delete_column('MODEL');
    add_soft_delete_column('PERSONEL_ZIMMET_HAREKET');
    add_soft_delete_column('SARF_LOG_KAYDI');
    add_soft_delete_column('SAYIM');
    add_soft_delete_column('SAYIM_DETAY');
    add_soft_delete_column('SQL_KAYDI');
END;
/
