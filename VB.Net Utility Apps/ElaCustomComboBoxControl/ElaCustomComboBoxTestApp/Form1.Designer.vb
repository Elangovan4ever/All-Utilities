<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class Form1
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
        Me.ElaCustomComboBox1 = New ElaCustomComboBoxControl.ElaCustomComboBox()
        Me.ElaCustomComboBox2 = New ElaCustomComboBoxControl.ElaCustomComboBox()
        Me.ElaCustomComboBox3 = New ElaCustomComboBoxControl.ElaCustomComboBox()
        Me.SuspendLayout()
        '
        'ElaCustomComboBox1
        '
        Me.ElaCustomComboBox1.ArrowSquareColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomComboBox1.ArrowSquareColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomComboBox1.ArrowSquareColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomComboBox1.ArrowTriangleColor = System.Drawing.Color.Gray
        Me.ElaCustomComboBox1.ArrowTriangleColorFocus = System.Drawing.Color.Gray
        Me.ElaCustomComboBox1.ArrowTriangleColorMouseEnter = System.Drawing.Color.White
        Me.ElaCustomComboBox1.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomComboBox1.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomComboBox1.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomComboBox1.BorderThickness = ElaCustomComboBoxControl.ElaCustomComboBox.BorderThicknessEnum.Thick
        Me.ElaCustomComboBox1.FormattingEnabled = True
        Me.ElaCustomComboBox1.Location = New System.Drawing.Point(304, 77)
        Me.ElaCustomComboBox1.Name = "ElaCustomComboBox1"
        Me.ElaCustomComboBox1.Size = New System.Drawing.Size(360, 21)
        Me.ElaCustomComboBox1.TabIndex = 0
        '
        'ElaCustomComboBox2
        '
        Me.ElaCustomComboBox2.ArrowSquareColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomComboBox2.ArrowSquareColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomComboBox2.ArrowSquareColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomComboBox2.ArrowTriangleColor = System.Drawing.Color.Gray
        Me.ElaCustomComboBox2.ArrowTriangleColorFocus = System.Drawing.Color.Gray
        Me.ElaCustomComboBox2.ArrowTriangleColorMouseEnter = System.Drawing.Color.White
        Me.ElaCustomComboBox2.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomComboBox2.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomComboBox2.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomComboBox2.BorderThickness = ElaCustomComboBoxControl.ElaCustomComboBox.BorderThicknessEnum.Thick
        Me.ElaCustomComboBox2.FormattingEnabled = True
        Me.ElaCustomComboBox2.Location = New System.Drawing.Point(304, 124)
        Me.ElaCustomComboBox2.Name = "ElaCustomComboBox2"
        Me.ElaCustomComboBox2.Size = New System.Drawing.Size(360, 21)
        Me.ElaCustomComboBox2.TabIndex = 1
        '
        'ElaCustomComboBox3
        '
        Me.ElaCustomComboBox3.ArrowSquareColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomComboBox3.ArrowSquareColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomComboBox3.ArrowSquareColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomComboBox3.ArrowTriangleColor = System.Drawing.Color.Gray
        Me.ElaCustomComboBox3.ArrowTriangleColorFocus = System.Drawing.Color.Gray
        Me.ElaCustomComboBox3.ArrowTriangleColorMouseEnter = System.Drawing.Color.White
        Me.ElaCustomComboBox3.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomComboBox3.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomComboBox3.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomComboBox3.BorderThickness = ElaCustomComboBoxControl.ElaCustomComboBox.BorderThicknessEnum.Thick
        Me.ElaCustomComboBox3.FormattingEnabled = True
        Me.ElaCustomComboBox3.Location = New System.Drawing.Point(304, 173)
        Me.ElaCustomComboBox3.Name = "ElaCustomComboBox3"
        Me.ElaCustomComboBox3.Size = New System.Drawing.Size(360, 21)
        Me.ElaCustomComboBox3.TabIndex = 2
        '
        'Form1
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.BackColor = System.Drawing.Color.White
        Me.ClientSize = New System.Drawing.Size(1001, 529)
        Me.Controls.Add(Me.ElaCustomComboBox3)
        Me.Controls.Add(Me.ElaCustomComboBox2)
        Me.Controls.Add(Me.ElaCustomComboBox1)
        Me.Name = "Form1"
        Me.Text = "Form1"
        Me.WindowState = System.Windows.Forms.FormWindowState.Maximized
        Me.ResumeLayout(False)

    End Sub

    Friend WithEvents ElaCustomComboBox1 As ElaCustomComboBoxControl.ElaCustomComboBox
    Friend WithEvents ElaCustomComboBox2 As ElaCustomComboBoxControl.ElaCustomComboBox
    Friend WithEvents ElaCustomComboBox3 As ElaCustomComboBoxControl.ElaCustomComboBox
End Class
